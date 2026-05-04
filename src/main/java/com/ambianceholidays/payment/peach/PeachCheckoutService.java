package com.ambianceholidays.payment.peach;

import com.ambianceholidays.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Peach Payments V2 Hosted Checkout — OAuth-authenticated REST API.
 *
 * Flow:
 *   1. PeachAuthService gets a Bearer token via /api/oauth/token using
 *      Client ID + Client Secret + Merchant ID.
 *   2. POST to {checkoutBaseUrl}/v2/checkout with the Bearer token.
 *      Body carries nested authentication.entityId + amount + currency +
 *      paymentType + merchantTransactionId + nonce + shopperResultUrl.
 *   3. Response gives us a checkoutId + redirectUrl. Frontend sends the
 *      browser to redirectUrl — Peach renders the hosted checkout page.
 *   4. Customer pays. Peach redirects the browser back to shopperResultUrl
 *      with ?id={checkoutId} as a query parameter.
 *   5. Frontend's PaymentReturnPage reads id and calls /payments/peach/status,
 *      which fetches the final result from Peach (GET /v2/checkout/{id}/payment).
 */
@Service
public class PeachCheckoutService {

    private static final Logger log = LoggerFactory.getLogger(PeachCheckoutService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Result codes per Peach docs: matches success codes returned by their result API. */
    private static final Pattern SUCCESS_PATTERN =
            Pattern.compile("^(000\\.000\\.|000\\.100\\.1|000\\.[36]|000\\.400\\.0[^3]|000\\.400\\.100)");

    /**
     * Result codes that mean the transaction is still being processed and the
     * outcome is not yet final. Customer should not be told the payment failed
     * — we keep Payment.PENDING and wait for a later webhook / poll. Per Peach
     * result-code reference: 000.200.* (pending), 800.400.5* (manual review),
     * 100.396.101 (cancelled by user is FAILED, not pending).
     */
    private static final Pattern PENDING_PATTERN =
            Pattern.compile("^(000\\.200\\.|800\\.400\\.5|100\\.400\\.500)");

    private final PeachProperties props;
    private final PeachAuthService auth;
    private final RestClient http;

    public PeachCheckoutService(PeachProperties props, PeachAuthService auth) {
        this.props = props;
        this.auth = auth;
        this.http = RestClient.builder().build();
    }

    public CreateResult createCheckout(int amountCents, String currency, String merchantTransactionId,
                                       String shopperResultUrl) {
        String url = trimTrailingSlash(props.getCheckoutBaseUrl()) + "/v2/checkout";
        String amount = BigDecimal.valueOf(amountCents)
                .movePointLeft(2)
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();

        Map<String, Object> body = new HashMap<>();
        body.put("authentication", Map.of("entityId", props.getEntityId()));
        body.put("amount",                amount);
        body.put("currency",              currency != null ? currency.toUpperCase() : props.getDefaultCurrency());
        body.put("paymentType",           "DB");
        body.put("merchantTransactionId", merchantTransactionId);
        body.put("nonce",                 UUID.randomUUID().toString());
        body.put("shopperResultUrl",      shopperResultUrl);
        body.put("defaultPaymentMethod",  "CARD");

        log.info("Peach V2 createCheckout {} ({} {}) → {}", merchantTransactionId, body.get("currency"), amount, url);

        String response = postJson(url, body);
        try {
            JsonNode json = MAPPER.readTree(response);
            String checkoutId  = firstText(json, "checkoutId", "id");
            String redirectUrl = firstText(json, "redirectUrl", "url");
            if (checkoutId == null || checkoutId.isBlank()) {
                log.error("Peach create-checkout response missing checkoutId: {}", response);
                throw BusinessException.badRequest("PEACH_CREATE_FAILED",
                        "Peach Payments did not return a checkout id");
            }
            log.info("Peach V2 checkout created: {} → {}", checkoutId, redirectUrl);
            return new CreateResult(checkoutId, redirectUrl);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Peach create-checkout response: {}", e.getMessage());
            throw BusinessException.badRequest("PEACH_CREATE_FAILED",
                    "Invalid Peach Payments create-checkout response");
        }
    }

    public StatusResult getStatus(String checkoutId) {
        String url = trimTrailingSlash(props.getCheckoutBaseUrl()) + "/v2/checkout/" + checkoutId + "/status";
        String response = getJson(url);
        try {
            JsonNode json = MAPPER.readTree(response);
            JsonNode result = json.path("result");
            String code = result.path("code").asText(null);
            String desc = result.path("description").asText(null);
            String paymentId = json.path("id").asText(null);
            boolean success = isSuccessCode(code);
            boolean pending = !success && isPendingCode(code);
            return new StatusResult(success, pending, code, desc, paymentId, response);
        } catch (Exception e) {
            log.error("Failed to parse Peach status response: {}", e.getMessage());
            throw BusinessException.badRequest("PEACH_STATUS_FAILED",
                    "Invalid Peach Payments status response");
        }
    }

    /** Inspect a Peach result code and return whether it indicates success. */
    public boolean isSuccessCode(String code) {
        return code != null && SUCCESS_PATTERN.matcher(code).find();
    }

    /** True if the code means "still processing"; outcome is not yet final. */
    public boolean isPendingCode(String code) {
        return code != null && PENDING_PATTERN.matcher(code).find();
    }

    private String postJson(String url, Object body) {
        // Peach's V2 API requires Origin / Referer matching a merchant
        // domain on file. In dev that's the public ngrok URL the backend
        // is reachable on (PEACH_BACKEND_BASE_URL); fall back to the SPA
        // URL otherwise. Strip any path so we send only the origin.
        String origin = originForRequest();
        try {
            return http.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + auth.getAccessToken())
                    .header("Content-Type", "application/json")
                    .header("Origin", origin)
                    .header("Referer", origin)
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            // 401 → token may be stale; invalidate and retry once.
            if (e.getStatusCode().value() == 401) {
                log.warn("Peach 401 — refreshing token and retrying once");
                auth.invalidate();
                return http.post()
                        .uri(url)
                        .header("Authorization", "Bearer " + auth.getAccessToken())
                        .header("Content-Type", "application/json")
                        .header("Origin", origin)
                        .header("Referer", origin)
                        .body(body)
                        .retrieve()
                        .body(String.class);
            }
            log.error("Peach API error {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw BusinessException.badRequest("PEACH_API_ERROR",
                    "Peach Payments rejected the request: " + e.getStatusCode().value());
        } catch (Exception e) {
            log.error("Peach API call failed: {}", e.getMessage());
            throw BusinessException.badRequest("PEACH_API_ERROR",
                    "Could not reach Peach Payments");
        }
    }

    private String getJson(String url) {
        String origin = originForRequest();
        try {
            return http.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + auth.getAccessToken())
                    .header("Origin", origin)
                    .header("Referer", origin)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                auth.invalidate();
                return http.get()
                        .uri(url)
                        .header("Authorization", "Bearer " + auth.getAccessToken())
                        .header("Origin", origin)
                        .header("Referer", origin)
                        .retrieve()
                        .body(String.class);
            }
            log.error("Peach status API error {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw BusinessException.badRequest("PEACH_STATUS_FAILED",
                    "Peach Payments returned " + e.getStatusCode().value());
        } catch (Exception e) {
            log.error("Peach status call failed: {}", e.getMessage());
            throw BusinessException.badRequest("PEACH_STATUS_FAILED",
                    "Could not reach Peach Payments");
        }
    }

    private static String firstText(JsonNode json, String... keys) {
        for (String k : keys) {
            JsonNode n = json.path(k);
            if (n != null && !n.isMissingNode() && !n.isNull() && !n.asText().isBlank()) return n.asText();
        }
        return null;
    }

    private static String trimTrailingSlash(String url) {
        if (url == null) return "";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String originForRequest() {
        // Prefer the publicly-reachable backend host (set in dev to the
        // ngrok URL, in prod to the public domain); fallback to the SPA URL.
        // Property wins over env var so application.yml is the source of truth;
        // env var is honoured as a fallback for legacy deploys.
        String configured = props.getBackendBaseUrl();
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("PEACH_BACKEND_BASE_URL");
        }
        String url = (configured != null && !configured.isBlank()) ? configured : props.getReturnBaseUrl();
        // Strip everything after the host (Origin must be scheme://host[:port], no path)
        try {
            java.net.URI u = java.net.URI.create(url);
            String scheme = u.getScheme() != null ? u.getScheme() : "https";
            String host = u.getHost() != null ? u.getHost() : url;
            int port = u.getPort();
            return scheme + "://" + host + (port > 0 ? ":" + port : "");
        } catch (Exception e) {
            return url;
        }
    }

    public record CreateResult(String checkoutId, String redirectUrl) {}
    public record StatusResult(boolean success, boolean pending, String code, String description,
                               String paymentId, String rawResponse) {}
}

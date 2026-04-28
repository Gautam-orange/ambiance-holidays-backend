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
 * Wraps the Peach Hosted Checkout V2 API: create a checkout, then check its status.
 */
@Service
public class PeachCheckoutService {

    private static final Logger log = LoggerFactory.getLogger(PeachCheckoutService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Result codes per Peach docs:
    //   /^(000\.000\.|000\.100\.1|000\.[36])/  → success
    //   /^(000\.400\.0|000\.400\.100)/         → success but manual review
    private static final Pattern SUCCESS_PATTERN =
            Pattern.compile("^(000\\.000\\.|000\\.100\\.1|000\\.[36]|000\\.400\\.0[^3]|000\\.400\\.100)");

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
        String url = props.getCheckoutBaseUrl() + "/v2/checkout";
        String amount = BigDecimal.valueOf(amountCents)
                .movePointLeft(2)
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();

        Map<String, Object> body = new HashMap<>();
        // Peach V2 takes nested "authentication" object
        body.put("authentication", Map.of("entityId", props.getEntityId()));
        body.put("amount",                amount);
        body.put("currency",              currency != null ? currency : props.getDefaultCurrency());
        body.put("paymentType",           "DB");      // Debit / direct charge
        body.put("merchantTransactionId", merchantTransactionId);
        body.put("nonce",                 UUID.randomUUID().toString());
        body.put("shopperResultUrl",      shopperResultUrl);
        body.put("defaultPaymentMethod",  "CARD");

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
        String url = props.getCheckoutBaseUrl() + "/v2/checkout/" + checkoutId + "/payment";
        String response = getJson(url);
        try {
            JsonNode json = MAPPER.readTree(response);
            JsonNode result = json.path("result");
            String code = result.path("code").asText(null);
            String desc = result.path("description").asText(null);
            String paymentId = json.path("id").asText(null);
            boolean success = code != null && SUCCESS_PATTERN.matcher(code).find();
            return new StatusResult(success, code, desc, paymentId, response);
        } catch (Exception e) {
            log.error("Failed to parse Peach status response: {}", e.getMessage());
            throw BusinessException.badRequest("PEACH_STATUS_FAILED",
                    "Invalid Peach Payments status response");
        }
    }

    private String postJson(String url, Object body) {
        try {
            return http.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + auth.getAccessToken())
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            // 401 → token may be stale; invalidate cache and retry once
            if (e.getStatusCode().value() == 401) {
                log.warn("Peach 401 — refreshing token and retrying once");
                auth.invalidate();
                return http.post()
                        .uri(url)
                        .header("Authorization", "Bearer " + auth.getAccessToken())
                        .header("Content-Type", "application/json")
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
        try {
            return http.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + auth.getAccessToken())
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                auth.invalidate();
                return http.get()
                        .uri(url)
                        .header("Authorization", "Bearer " + auth.getAccessToken())
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

    public record CreateResult(String checkoutId, String redirectUrl) {}
    public record StatusResult(boolean success, String code, String description,
                               String paymentId, String rawResponse) {}
}

package com.ambianceholidays.payment.peach;

import com.ambianceholidays.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

/**
 * Caches a Peach OAuth2 bearer token in Redis. Refreshes when expired.
 */
@Service
public class PeachAuthService {

    private static final Logger log = LoggerFactory.getLogger(PeachAuthService.class);
    private static final String CACHE_KEY = "peach:oauth:token";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final PeachProperties props;
    private final StringRedisTemplate redis;
    private final RestClient http;

    public PeachAuthService(PeachProperties props, StringRedisTemplate redis) {
        this.props = props;
        this.redis = redis;
        this.http = RestClient.builder().build();
    }

    public String getAccessToken() {
        if (!props.isConfigured()) {
            throw BusinessException.badRequest("PEACH_NOT_CONFIGURED",
                    "Peach Payments credentials are not configured");
        }
        // Hosted Checkout (Bearer) flow — the secret token IS the bearer.
        // No OAuth handshake needed. This matches the "Hosted Checkout" block
        // in the Peach dashboard (Entity ID + Secret token) and works without
        // the merchant having OAuth/Embedded Checkout activated.
        if (props.useBearer()) {
            return props.getSecretToken();
        }
        // Embedded Checkout (OAuth) flow — fetch + cache the access token.
        String cached = redis.opsForValue().get(CACHE_KEY);
        if (cached != null && !cached.isBlank()) return cached;
        return fetchAndCache();
    }

    public void invalidate() {
        redis.delete(CACHE_KEY);
    }

    private String fetchAndCache() {
        String url = props.getAuthBaseUrl() + "/api/oauth/token";
        Map<String, String> body = Map.of(
                "clientId",     props.getClientId(),
                "clientSecret", props.getClientSecret(),
                "merchantId",   props.getMerchantId()
        );

        log.info("Requesting Peach OAuth token from {}", url);
        // Debug: log first/last 6 chars of each credential so we can confirm the
        // request actually carries what's configured (without leaking full secrets).
        String cid = props.getClientId();
        String csec = props.getClientSecret();
        String mid = props.getMerchantId();
        log.info("  clientId len={} prefix={} suffix={}",
                cid.length(), cid.substring(0, Math.min(6, cid.length())),
                cid.length() > 6 ? cid.substring(cid.length() - 6) : "");
        log.info("  clientSecret len={} prefix={} suffix={}",
                csec.length(), csec.substring(0, Math.min(6, csec.length())),
                csec.length() > 6 ? csec.substring(csec.length() - 6) : "");
        log.info("  merchantId len={} prefix={} suffix={}",
                mid.length(), mid.substring(0, Math.min(6, mid.length())),
                mid.length() > 6 ? mid.substring(mid.length() - 6) : "");

        String response;
        try {
            response = http.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.error("Peach OAuth call failed: {}", e.getMessage());
            throw BusinessException.badRequest("PEACH_AUTH_FAILED",
                    "Could not authenticate with Peach Payments: " + e.getMessage());
        }

        try {
            JsonNode json = MAPPER.readTree(response);
            String token   = firstNonNull(json.path("access_token").asText(null),
                                          json.path("accessToken").asText(null));
            long expiresIn = firstLong(json.path("expires_in").asLong(0),
                                       json.path("expiresIn").asLong(0));
            if (token == null || token.isBlank()) {
                log.error("Peach OAuth response did not include access_token: {}", response);
                throw BusinessException.badRequest("PEACH_AUTH_FAILED",
                        "Peach Payments authentication response missing access_token");
            }
            // Cache slightly less than the real expiry so we always refresh before it dies
            long cacheSeconds = Math.max(60, (expiresIn > 0 ? expiresIn : 1800) - 60);
            redis.opsForValue().set(CACHE_KEY, token, Duration.ofSeconds(cacheSeconds));
            log.info("Peach OAuth token cached ({}s)", cacheSeconds);
            return token;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Peach OAuth response: {}", e.getMessage());
            throw BusinessException.badRequest("PEACH_AUTH_FAILED",
                    "Invalid Peach Payments authentication response");
        }
    }

    private static String firstNonNull(String a, String b) { return a != null ? a : b; }
    private static long   firstLong(long a, long b)        { return a != 0    ? a : b; }
}

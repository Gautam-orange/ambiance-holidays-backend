package com.ambianceholidays.payment.peach;

import com.ambianceholidays.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Peach Payments V1 Hosted Checkout — HMAC-SHA256 signed flow.
 *
 * Why V1 not V2:
 *   The merchant's "Hosted Checkout" credentials in the Peach dashboard
 *   (Entity ID + Secret token) are V1 credentials. V2 (OAuth) requires
 *   Embedded Checkout activation which isn't available on every sandbox.
 *
 * Flow:
 *   1. Backend builds the form params, computes the HMAC signature,
 *      and returns them to the frontend via /payments/peach/initiate.
 *   2. Frontend constructs an HTML form pointing at testsecure.peachpayments.com/checkout
 *      and auto-submits — the browser POSTs to Peach.
 *   3. Peach renders the hosted checkout page. Customer pays.
 *   4. Peach POSTs the result back to shopperResultUrl (our /return endpoint).
 *   5. Backend updates the booking and redirects the browser to a result page.
 *
 * Signature spec (per Peach docs):
 *   message = sortedKeys.map(k -> k + value(k)).join("")
 *   signature = HMAC-SHA256(message, secretToken) → hex lowercase
 */
@Service
public class PeachCheckoutService {

    private static final Logger log = LoggerFactory.getLogger(PeachCheckoutService.class);

    /** Per Peach docs: matches success codes returned by their result API. */
    private static final Pattern SUCCESS_PATTERN =
            Pattern.compile("^(000\\.000\\.|000\\.100\\.1|000\\.[36]|000\\.400\\.0[^3]|000\\.400\\.100)");

    private final PeachProperties props;

    public PeachCheckoutService(PeachProperties props) {
        this.props = props;
    }

    /**
     * Build the signed form params the frontend needs to POST to Peach.
     *
     * @param amountCents             total amount in cents
     * @param currency                ISO currency code (e.g. USD)
     * @param merchantTransactionId   our booking reference (e.g. AMB-123456)
     * @param shopperResultUrl        URL Peach POSTs back to once the
     *                                customer finishes (success or failure)
     */
    public CreateResult createCheckout(int amountCents, String currency, String merchantTransactionId,
                                       String shopperResultUrl) {
        if (props.getEntityId() == null || props.getEntityId().isBlank()
                || props.getSecretToken() == null || props.getSecretToken().isBlank()) {
            throw BusinessException.badRequest("PEACH_NOT_CONFIGURED",
                    "Peach Payments Entity ID and Secret Token must be configured");
        }

        String amount = BigDecimal.valueOf(amountCents)
                .movePointLeft(2)
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();

        String resolvedCurrency = currency != null ? currency.toUpperCase() : props.getDefaultCurrency();
        String nonce = UUID.randomUUID().toString();

        // Params that get signed — alphabetical order matters for the signature
        // computation but order is irrelevant on submit (Peach reads them by name).
        Map<String, String> params = new TreeMap<>();
        params.put("amount", amount);
        params.put("authentication.entityId", props.getEntityId());
        params.put("currency", resolvedCurrency);
        params.put("defaultPaymentMethod", "CARD");
        params.put("merchantTransactionId", merchantTransactionId);
        params.put("nonce", nonce);
        params.put("paymentType", "DB");
        params.put("shopperResultUrl", shopperResultUrl);

        String signature = computeSignature(params, props.getSecretToken());

        // Result preserves a stable insertion order for the frontend form fields.
        Map<String, String> formFields = new LinkedHashMap<>(params);
        formFields.put("signature", signature);

        // Submission URL = checkoutBaseUrl/checkout. For sandbox V1 this is
        // https://testsecure.peachpayments.com/checkout (the Form-POST variant).
        String submitUrl = trimTrailingSlash(props.getCheckoutBaseUrl()) + "/checkout";

        log.info("Peach V1 checkout params built for {} ({} {}) — submit to {}",
                merchantTransactionId, resolvedCurrency, amount, submitUrl);

        // V1 doesn't return a checkoutId from a server-side call. We use the
        // merchantTransactionId (= booking reference) as our local correlation
        // key and treat it as the checkoutId for downstream code.
        return new CreateResult(merchantTransactionId, submitUrl, formFields);
    }

    /**
     * Verify a signature returned by Peach in the result POST. Same algorithm
     * as createCheckout, but excludes the `signature` key itself from the message.
     */
    public boolean verifyResultSignature(Map<String, String> params, String receivedSignature) {
        if (receivedSignature == null || receivedSignature.isBlank()) return false;
        Map<String, String> sorted = new TreeMap<>(params);
        sorted.remove("signature");
        String expected = computeSignature(sorted, props.getSecretToken());
        // Constant-time compare
        if (expected.length() != receivedSignature.length()) return false;
        int diff = 0;
        for (int i = 0; i < expected.length(); i++) {
            diff |= expected.charAt(i) ^ receivedSignature.charAt(i);
        }
        return diff == 0;
    }

    /**
     * Inspect a Peach result code (e.g. "000.100.110") and return whether it
     * indicates a successful payment per the published regex.
     */
    public boolean isSuccessCode(String code) {
        return code != null && SUCCESS_PATTERN.matcher(code).find();
    }

    private static String computeSignature(Map<String, String> sortedParams, String secretToken) {
        StringBuilder message = new StringBuilder();
        for (Map.Entry<String, String> e : sortedParams.entrySet()) {
            message.append(e.getKey()).append(e.getValue() == null ? "" : e.getValue());
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretToken.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(message.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to compute Peach HMAC signature", ex);
        }
    }

    private static String trimTrailingSlash(String url) {
        if (url == null) return "";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * @param checkoutId  In V1 we use the merchantTransactionId as the local id (Peach has no separate checkoutId)
     * @param submitUrl   The URL the frontend should POST the form to
     * @param formFields  All form fields including the signature — keys are
     *                    Peach's parameter names (e.g. "authentication.entityId")
     */
    public record CreateResult(String checkoutId, String submitUrl, Map<String, String> formFields) {}

    /** Result of a Peach status check (used by the /return endpoint and /status polling). */
    public record StatusResult(boolean success, String code, String description,
                               String paymentId, String rawResponse) {}
}

package com.ambianceholidays.payment.peach;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.peach")
public class PeachProperties {
    private String clientId;
    private String clientSecret;
    private String merchantId;
    private String entityId;
    /**
     * Peach "Hosted Checkout" Secret Token. When set, the backend uses simple
     * Bearer auth and skips the OAuth handshake entirely. This matches the
     * "Hosted Checkout" credential block in the dashboard (Entity ID + Secret
     * token), which is the simpler/more compatible flow for V2 redirect-style
     * checkout. If not set, the OAuth (Embedded Checkout) flow is used.
     */
    private String secretToken;
    private String authBaseUrl;
    private String checkoutBaseUrl;
    private String backendBaseUrl;
    private String returnBaseUrl;
    private String defaultCurrency = "USD";

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getSecretToken() { return secretToken; }
    public void setSecretToken(String secretToken) { this.secretToken = secretToken; }
    public String getAuthBaseUrl() { return authBaseUrl; }
    public void setAuthBaseUrl(String authBaseUrl) { this.authBaseUrl = authBaseUrl; }
    public String getCheckoutBaseUrl() { return checkoutBaseUrl; }
    public void setCheckoutBaseUrl(String checkoutBaseUrl) { this.checkoutBaseUrl = checkoutBaseUrl; }
    public String getBackendBaseUrl() { return backendBaseUrl; }
    public void setBackendBaseUrl(String backendBaseUrl) { this.backendBaseUrl = backendBaseUrl; }
    public String getReturnBaseUrl() { return returnBaseUrl; }
    public void setReturnBaseUrl(String returnBaseUrl) { this.returnBaseUrl = returnBaseUrl; }
    public String getDefaultCurrency() { return defaultCurrency; }
    public void setDefaultCurrency(String defaultCurrency) { this.defaultCurrency = defaultCurrency; }

    /** True if either Bearer (secretToken) or OAuth (clientId/secret/merchantId) credentials are set. */
    public boolean isConfigured() {
        if (entityId == null || entityId.isBlank()) return false;
        boolean hasBearer = secretToken != null && !secretToken.isBlank();
        boolean hasOAuth = clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank()
                && merchantId != null && !merchantId.isBlank();
        return hasBearer || hasOAuth;
    }

    /** True when the simpler Bearer (Hosted Checkout) flow should be used. */
    public boolean useBearer() {
        return secretToken != null && !secretToken.isBlank();
    }
}

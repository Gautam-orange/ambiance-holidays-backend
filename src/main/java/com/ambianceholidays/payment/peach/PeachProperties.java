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
    private String authBaseUrl;
    private String checkoutBaseUrl;
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
    public String getAuthBaseUrl() { return authBaseUrl; }
    public void setAuthBaseUrl(String authBaseUrl) { this.authBaseUrl = authBaseUrl; }
    public String getCheckoutBaseUrl() { return checkoutBaseUrl; }
    public void setCheckoutBaseUrl(String checkoutBaseUrl) { this.checkoutBaseUrl = checkoutBaseUrl; }
    public String getReturnBaseUrl() { return returnBaseUrl; }
    public void setReturnBaseUrl(String returnBaseUrl) { this.returnBaseUrl = returnBaseUrl; }
    public String getDefaultCurrency() { return defaultCurrency; }
    public void setDefaultCurrency(String defaultCurrency) { this.defaultCurrency = defaultCurrency; }

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank()
            && clientSecret != null && !clientSecret.isBlank()
            && merchantId != null && !merchantId.isBlank()
            && entityId != null && !entityId.isBlank();
    }
}

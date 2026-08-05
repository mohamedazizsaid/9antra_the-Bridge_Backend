package com._antra.the_bridge.dto;

public class StripeCheckoutResponse {

    private String sessionId;
    private String url;
    private String status;

    public StripeCheckoutResponse() {}

    public StripeCheckoutResponse(String sessionId, String url, String status) {
        this.sessionId = sessionId;
        this.url = url;
        this.status = status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

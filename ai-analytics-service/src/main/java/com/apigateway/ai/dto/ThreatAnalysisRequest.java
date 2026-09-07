package com.apigateway.ai.dto;

import java.util.Map;

public class ThreatAnalysisRequest {
    private String clientIp;
    private String endpoint;
    private String httpMethod;
    private String payload;
    private Map<String, String> headers;
    private Map<String, String> queryParams;

    public ThreatAnalysisRequest() {}

    public ThreatAnalysisRequest(String clientIp, String endpoint, String httpMethod, String payload, Map<String, String> headers, Map<String, String> queryParams) {
        this.clientIp = clientIp;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.payload = payload;
        this.headers = headers;
        this.queryParams = queryParams;
    }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public Map<String, String> getQueryParams() { return queryParams; }
    public void setQueryParams(Map<String, String> queryParams) { this.queryParams = queryParams; }
}

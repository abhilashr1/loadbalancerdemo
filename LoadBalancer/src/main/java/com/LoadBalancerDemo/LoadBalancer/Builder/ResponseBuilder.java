package com.LoadBalancerDemo.LoadBalancer.Builder;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class ResponseBuilder {
    private final ResponseEntity<String> originalResponse;
    private final HttpHeaders headers;

    private ResponseBuilder(ResponseEntity<String> response) {
        this.originalResponse = response;
        this.headers = new HttpHeaders();
    }

    public static ResponseBuilder from(ResponseEntity<String> response) {
        return new ResponseBuilder(response);
    }

    public ResponseBuilder copyHeaders() {
        headers.putAll(originalResponse.getHeaders());
        headers.remove(HttpHeaders.TRANSFER_ENCODING);
        return this;
    }

    public ResponseBuilder setContentLength() {
        String responseBody = originalResponse.getBody();
        if (responseBody != null) {
            headers.setContentLength(responseBody.length());
        }
        return this;
    }

    public ResponseEntity<String> build() {
        return ResponseEntity
            .status(originalResponse.getStatusCode())
            .headers(headers)
            .body(originalResponse.getBody());
    }
} 
package com.LoadBalancerDemo.LoadBalancer.Builder;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.Enumeration;

public class RequestBuilder {
    private final HttpServletRequest originalRequest;
    private final byte[] body;
    private final HttpHeaders headers;

    private RequestBuilder(HttpServletRequest request, byte[] body) {
        this.originalRequest = request;
        this.body = body;
        this.headers = new HttpHeaders();
    }

    public static RequestBuilder from(HttpServletRequest request, byte[] body) {
        return new RequestBuilder(request, body);
    }

    public RequestBuilder copyHeaders() {
        Enumeration<String> headerNames = originalRequest.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = originalRequest.getHeader(headerName);
            headers.add(headerName, headerValue);
        }
        return this;
    }

    public String getPath() {
        return originalRequest.getRequestURI();
    }

    public String getMethod() {
        return originalRequest.getMethod();
    }

    public HttpEntity<String> build() {
        return new HttpEntity<>(
            body != null ? new String(body) : "",
            headers
        );
    }
} 
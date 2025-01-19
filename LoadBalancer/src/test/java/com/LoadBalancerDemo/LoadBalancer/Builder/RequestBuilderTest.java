package com.LoadBalancerDemo.LoadBalancer.Builder;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestBuilderTest {
    @Test
    void buildsRequestWithHeaders() {
        var request = mock(HttpServletRequest.class);
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(List.of("Content-Type")));
        when(request.getHeader("Content-Type")).thenReturn("application/json");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("POST");

        var body = "{ \"test\": true }".getBytes();

        var result = RequestBuilder.from(request, body)
            .copyHeaders()
            .build();

        assertEquals("application/json", result.getHeaders().getFirst("Content-Type"));
        assertTrue(result.getBody().contains("test"));
    }
} 
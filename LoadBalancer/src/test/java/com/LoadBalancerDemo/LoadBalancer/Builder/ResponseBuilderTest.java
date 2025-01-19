package com.LoadBalancerDemo.LoadBalancer.Builder;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class ResponseBuilderTest {
    @Test
    void build_CreatesResponseWithHeadersAndBody() {
        HttpHeaders originalHeaders = new HttpHeaders();
        originalHeaders.add("Custom-Header", "abhi123");
        originalHeaders.add(HttpHeaders.TRANSFER_ENCODING, "chunked");
        
        ResponseEntity<String> originalResponse = ResponseEntity
            .status(HttpStatus.OK)
            .headers(originalHeaders)
            .body("sample response");

        ResponseEntity<String> result = ResponseBuilder.from(originalResponse)
            .copyHeaders()
            .setContentLength()
            .build();

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("sample response", result.getBody());
        assertEquals("abhi123", result.getHeaders().getFirst("Custom-Header"));
        assertNull(result.getHeaders().getFirst(HttpHeaders.TRANSFER_ENCODING));
        assertEquals(15L, result.getHeaders().getContentLength());
    }
} 
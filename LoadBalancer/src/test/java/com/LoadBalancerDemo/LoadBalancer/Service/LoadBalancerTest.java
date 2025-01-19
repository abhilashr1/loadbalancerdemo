package com.LoadBalancerDemo.LoadBalancer.Service;

import com.LoadBalancerDemo.LoadBalancer.Factory.ServerFactory;
import com.LoadBalancerDemo.LoadBalancer.Factory.StrategyFactory;
import com.LoadBalancerDemo.LoadBalancer.Models.Server;
import com.LoadBalancerDemo.LoadBalancer.Service.BalancingStrategies.ILoadBalancingStrategy;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoadBalancerTest {
    @Mock private RestTemplate restTemplate;
    @Mock private StrategyFactory strategyFactory;
    @Mock private ILoadBalancingStrategy strategy;
    @Mock private HttpServletRequest request;

    private LoadBalancer loadBalancer;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        meterRegistry = new SimpleMeterRegistry();
        
        List<Server> servers = List.of(
            new Server("http://server1"),
            new Server("http://server2")
        );

        when(strategyFactory.createStrategy()).thenReturn(strategy);
        when(strategy.getNextServer(any())).thenReturn(servers.get(0));
        
        loadBalancer = new LoadBalancer(restTemplate, strategyFactory, servers, meterRegistry);
    }

    @Test
    void shouldForwardRequestSuccessfully() throws ExecutionException, InterruptedException {
        mockBasicRequest();
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        ResponseEntity<String> response = loadBalancer.forwardRequest(null, request).get();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());
        assertEquals(1.0, meterRegistry.get("loadbalancer.requests.total").counter().count());
    }

    @Test
    void shouldHandleServerError() {
        mockBasicRequest();
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenThrow(new RuntimeException("Server Error"));

        assertThrows(ExecutionException.class, 
            () -> loadBalancer.forwardRequest(null, request).get());
        verify(strategy).handleServerFailure(any(Server.class));
    }

    private void mockBasicRequest() {
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getMethod()).thenReturn("GET");
    }
}
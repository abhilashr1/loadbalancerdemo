package com.LoadBalancerDemo.LoadBalancer.Service;

import com.LoadBalancerDemo.LoadBalancer.Config.LoadBalancerProperties;
import com.LoadBalancerDemo.LoadBalancer.Models.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class HealthCheckServiceTest {
    @Mock private LoadBalancerProperties props;
    @Mock private LoadBalancerProperties.Health healthConfig;
    private HealthCheckService service;
    private List<Server> servers;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(props.getHealth()).thenReturn(healthConfig);
        when(healthConfig.getSlowResponseThresholdMs()).thenReturn(1200L);
        when(healthConfig.getSlowThresholdCount()).thenReturn(3);

        servers = List.of(new Server("localhost:8080"));
        service = new HealthCheckService(servers, props);
    }

    @Test
    void marksUnhealthyWhenSlow() {
        var server = servers.get(0);
        server.addResponseTime(1250);
        server.addResponseTime(1300);
        server.addResponseTime(1280);

        service.checkServerHealth();
        assertFalse(server.isHealthy());
    }

    @Test
    void checkServerHealth_StaysHealthyWhenResponsesAreGood() {
        Server server = servers.get(0);
        server.addResponseTime(500);
        server.addResponseTime(500);

        service.checkServerHealth();

        assertTrue(server.isHealthy());
    }
} 
package com.LoadBalancerDemo.LoadBalancer.Models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {
    private Server server;

    @BeforeEach
    void setUp() {
        server = new Server("http://server:8080");
    }

    @Test
    void shouldResetServerStateOnReset() {
        server.setUnhealthyFromError();
        server.incrementErrors();
        server.addResponseTime(1000);
        
        server.resetFailCount();

        assertTrue(server.isHealthy());
        assertEquals(0, server.getErrors());
    }

    @Test
    void shouldKeepOnlyLastTenResponseTimes() {
        for (int i = 0; i < 100; i++) {
            server.addResponseTime(i * 1000);
        }

        assertEquals(10, server.getRecentResponseTimes().size());
        assertTrue(server.getRecentResponseTimes().stream()
                .allMatch(t -> t >= 90000));
    }
} 
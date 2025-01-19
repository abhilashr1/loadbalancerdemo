package com.LoadBalancerDemo.LoadBalancer.Service.BalancingStrategies;

import com.LoadBalancerDemo.LoadBalancer.Models.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoundRobinStrategyTest {
    private RoundRobinStrategy strategy;
    private List<Server> servers;

    @BeforeEach
    void setUp() {
        strategy = new RoundRobinStrategy();
        servers = List.of(
            new Server("http://localhost:8081"),
            new Server("http://localhost:8082")
        );
    }

    @Test
    void rotatesThroughAvailableServers() {
        var first = strategy.getNextServer(servers);
        var second = strategy.getNextServer(servers);
        var third = strategy.getNextServer(servers);
        
        assertEquals(first.getUrl(), third.getUrl());
        assertNotEquals(first.getUrl(), second.getUrl());
    }

    @Test
    void skipsUnhealthyServer() {
        servers.get(0).setHealthy(false);
        var server = strategy.getNextServer(servers);
        
        assertEquals("http://localhost:8082", server.getUrl());
    }

    @Test
    void marksUnhealthyAfterErrors() {
        var server = servers.get(0);
        
        strategy.handleServerFailure(server);
        strategy.handleServerFailure(server);
        strategy.handleServerFailure(server);

        assertFalse(server.isHealthy());
    }
} 
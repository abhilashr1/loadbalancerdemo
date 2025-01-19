package com.LoadBalancerDemo.LoadBalancer.Service.BalancingStrategies;

import com.LoadBalancerDemo.LoadBalancer.Service.LoadBalancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.LoadBalancerDemo.LoadBalancer.Models.Server;

@Component
public class RoundRobinStrategy implements ILoadBalancingStrategy {
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private static final Logger logger = LoggerFactory.getLogger(LoadBalancer.class);

    @Override
    public Server getNextServer(List<Server> servers) {
        int attempts = 0;
        while (attempts < servers.size()) {
            int index = currentIndex.getAndIncrement() % servers.size();
            Server server = servers.get(index);

            if (isServerAvailable(server)) {
                return server;
            }
            attempts++;
        }
        logger.error("No healthy servers available");
        throw new RuntimeException("No healthy servers available");
    }

    private boolean isServerAvailable(Server server) {
        if (!server.isHealthy()) {
            if (server.getLastFailureFromError() != null &&
                    Duration.between(server.getLastFailureFromError(), LocalDateTime.now()).getSeconds() > 30) {
                server.resetFailCount();
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public void handleServerFailure(Server server) {
        server.incrementErrors();
        
        if (server.getErrors() >= 3) {
            server.setUnhealthyFromError();
        }
    }
}
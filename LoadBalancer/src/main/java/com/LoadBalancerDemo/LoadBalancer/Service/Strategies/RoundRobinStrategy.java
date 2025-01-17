package com.LoadBalancerDemo.LoadBalancer.Service.Strategies;

import com.LoadBalancerDemo.LoadBalancer.Models.Server;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RoundRobinStrategy implements ILoadBalancingStrategy {
    private final AtomicInteger currentIndex = new AtomicInteger(0);

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
        throw new RuntimeException("No healthy servers available");
    }

    private boolean isServerAvailable(Server server) {
        if (!server.isHealthy()) {
            if (server.getLastFailure() != null &&
                    Duration.between(server.getLastFailure(), LocalDateTime.now()).getSeconds() > 30) {
                server.resetFailCount();
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public void handleServerFailure(Server server) {
        server.setHealthy(false);
    }
}
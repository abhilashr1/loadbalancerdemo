package com.LoadBalancerDemo.LoadBalancer.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.LoadBalancerDemo.LoadBalancer.Config.LoadBalancerProperties;
import com.LoadBalancerDemo.LoadBalancer.Models.Server;

@Service
public class HealthCheckService {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckService.class);
    private final List<Server> servers;
    private final LoadBalancerProperties properties;

    public HealthCheckService(List<Server> servers, LoadBalancerProperties properties) {
        this.servers = servers;
        this.properties = properties;
    }

    @Scheduled(fixedRateString = "${loadbalancer.health.slowCheckInterval:5000}")
    public void checkServerHealth() {
        logger.debug("Starting health check for {} servers", servers.size());
        
        for (Server server : servers) {
            boolean isServerDown = !server.isHealthy() && server.getLastFailureFromSlowResponse() != null;

            if (isServerDown) {
                long cooldownMs = properties.getHealth().getSlowCooldownMs();
                boolean hasCooldownTimePassed = LocalDateTime.now().minus(cooldownMs, ChronoUnit.MILLIS)
                        .isAfter(server.getLastFailureFromSlowResponse());

                if (hasCooldownTimePassed) {
                    logger.info("Reactivating server {} after cooldown", server.getUrl());
                    server.setHealthy();
                    continue;
                }
            }

            Queue<Long> responseTimes = server.getRecentResponseTimes();
            boolean hasEnoughResponses = responseTimes.size() >= properties.getHealth().getSlowThresholdCount();

            if (hasEnoughResponses) {
                long slowResponses = responseTimes.stream()
                        .filter(time -> time > properties.getHealth().getSlowResponseThresholdMs())
                        .count();

                boolean isUnhealthy = slowResponses >= properties.getHealth().getSlowThresholdCount();
                if (isUnhealthy) {
                    logger.warn("Server {} marked as unhealthy due to {} slow responses", 
                              server.getUrl(), slowResponses);
                    server.setUnhealthyFromSlowResponse();
                }
            }
        }
        
        logger.debug("Health check completed");
    }
} 
package com.LoadBalancerDemo.LoadBalancer.Service;

import com.LoadBalancerDemo.LoadBalancer.Config.LoadBalancerProperties;
import com.LoadBalancerDemo.LoadBalancer.Models.Server;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Queue;

@Service
public class HealthCheckService {
    private final List<Server> servers;
    private final LoadBalancerProperties properties;

    public HealthCheckService(List<Server> servers, LoadBalancerProperties properties) {
        this.servers = servers;
        this.properties = properties;
    }

    @Scheduled(fixedRateString = "${loadbalancer.health.checkInterval:5000}")
    public void checkServerHealth() {
        for (Server server : servers) {
            Queue<Long> responseTimes = server.getRecentResponseTimes();
            
            if (responseTimes.size() >= properties.getHealth().getSlowThresholdCount()) {
                long slowResponses = responseTimes.stream()
                        .filter(time -> time > properties.getHealth().getResponseThresholdMs())
                        .count();

                if (slowResponses >= properties.getHealth().getSlowThresholdCount()) {
                    server.setHealthy(false);
                }
            }
        }
    }
} 
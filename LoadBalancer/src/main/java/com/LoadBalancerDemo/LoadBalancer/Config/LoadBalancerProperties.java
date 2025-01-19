package com.LoadBalancerDemo.LoadBalancer.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "loadbalancer")
public class LoadBalancerProperties {

    private String strategy;
    private List<String> servers;
    private HealthConfig health = new HealthConfig();

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public List<String> getServers() {
        return servers;
    }

    public void setServers(List<String> servers) {
        this.servers = servers;
    }

    public HealthConfig getHealth() {
        return health;
    }

    public void setHealth(HealthConfig health) {
        this.health = health;
    }

    public static class HealthConfig {
        private final int DEFAULT_RESPONSE_THRESHOLD_MS = 10000;
        private final int DEFAULT_SLOW_THRESHOLD_COUNT = 10;
        private final int DEFAULT_CHECK_INTERVAL = 5000;

        private long responseThresholdMs = DEFAULT_RESPONSE_THRESHOLD_MS;
        private int slowThresholdCount = DEFAULT_SLOW_THRESHOLD_COUNT;
        private long checkInterval = DEFAULT_CHECK_INTERVAL;

        public long getResponseThresholdMs() {
            return responseThresholdMs;
        }

        public void setResponseThresholdMs(long responseThresholdMs) {
            this.responseThresholdMs = responseThresholdMs;
        }

        public int getSlowThresholdCount() {
            return slowThresholdCount;
        }

        public void setSlowThresholdCount(int slowThresholdCount) {
            this.slowThresholdCount = slowThresholdCount;
        }

        public long getCheckInterval() {
            return checkInterval;
        }

        public void setCheckInterval(long checkInterval) {
            this.checkInterval = checkInterval;
        }
    }
}
package com.LoadBalancerDemo.LoadBalancer.Config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "loadbalancer")
public class LoadBalancerProperties {

    private String strategy;
    private List<String> servers;
    private Health health = new Health();

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

    public Health getHealth() {
        return health;
    }

    public void setHealth(Health health) {
        this.health = health;
    }

    public static class Health {
        private final int DEFAULT_SLOW_RESPONSE_THRESHOLD_MS = 10000;
        private final int DEFAULT_SLOW_THRESHOLD_COUNT = 10;
        private final int DEFAULT_SLOW_CHECK_INTERVAL = 5000;
        private final int DEFAULT_SLOW_COOLDOWN_MS = 5;

        private long slowResponseThresholdMs = DEFAULT_SLOW_RESPONSE_THRESHOLD_MS;
        private int slowThresholdCount = DEFAULT_SLOW_THRESHOLD_COUNT;
        private long slowCheckInterval = DEFAULT_SLOW_CHECK_INTERVAL;
        private long slowCooldownMs = DEFAULT_SLOW_COOLDOWN_MS;

        public long getSlowResponseThresholdMs() {
            return slowResponseThresholdMs;
        }

        public void setSlowResponseThresholdMs(long responseThresholdMs) {
            this.slowResponseThresholdMs = responseThresholdMs;
        }

        public int getSlowThresholdCount() {
            return slowThresholdCount;
        }

        public void setSlowThresholdCount(int slowThresholdCount) {
            this.slowThresholdCount = slowThresholdCount;
        }

        public long getSlowCheckInterval() {
            return slowCheckInterval;
        }

        public void setSlowCheckInterval(long checkInterval) {
            this.slowCheckInterval = checkInterval;
        }

        public long getSlowCooldownMs() {
            return slowCooldownMs;
        }

        public void setSlowCooldownMs(long cooldownMs) {
            this.slowCooldownMs = cooldownMs;
        }
    }
}
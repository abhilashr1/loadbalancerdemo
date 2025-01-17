package com.LoadBalancerDemo.LoadBalancer.Models;

import java.time.LocalDateTime;

public class Server {
    private final String url;
    private boolean healthy = true;
    private LocalDateTime lastFailure;

    public Server(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
        if (!healthy) {
            lastFailure = LocalDateTime.now();
        }
    }

    public LocalDateTime getLastFailure() {
        return lastFailure;
    }

    public void resetFailCount() {
        this.healthy = true;
    }
}


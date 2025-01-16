package com.LoadBalancerDemo.LoadBalancer.Models;

import java.time.LocalDateTime;

public class Server {
    private final String url;
    private boolean healthy = true;
    private int failCount = 0;
    private LocalDateTime lastFailure;
    private long responseTime = 0;

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
            failCount++;
            lastFailure = LocalDateTime.now();
        }
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public int getFailCount() {
        return failCount;
    }

    public LocalDateTime getLastFailure() {
        return lastFailure;
    }

    public void resetFailCount() {
        this.failCount = 0;
        this.healthy = true;
    }
}


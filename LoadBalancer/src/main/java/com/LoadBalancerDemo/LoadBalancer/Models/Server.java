package com.LoadBalancerDemo.LoadBalancer.Models;

import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private final int MAX_RECENT_RESPONSE_TIMES = 10;

    private final String url;
    private LocalDateTime lastFailure;

    private final AtomicBoolean healthy = new AtomicBoolean(true);
    private final AtomicInteger consecutiveErrors = new AtomicInteger(0);

    private final Queue<Long> recentResponseTimes = new ConcurrentLinkedQueue<>();

    public Server(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public boolean isHealthy() {
        return healthy.get();
    }

    public void setHealthy(boolean healthy) {
        this.healthy.set(healthy);
        if (!healthy) {
            lastFailure = LocalDateTime.now();
        }
    }

    public LocalDateTime getLastFailure() {
        return lastFailure;
    }

    public void resetFailCount() {
        this.healthy.set(true);
        this.consecutiveErrors.set(0);
        this.recentResponseTimes.clear();
    }

    public void addResponseTime(long responseTime) {
        recentResponseTimes.offer(responseTime);
        while (recentResponseTimes.size() > MAX_RECENT_RESPONSE_TIMES) {
            recentResponseTimes.poll();
        }
    }

    public void incrementErrors() {
        consecutiveErrors.incrementAndGet();
    }

    public int getConsecutiveErrors() {
        return consecutiveErrors.get();
    }

    public Queue<Long> getRecentResponseTimes() {
        return recentResponseTimes;
    }
}


package com.LoadBalancerDemo.LoadBalancer.Models;

import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final int RESPONSE_HISTORY_SIZE = 10;

    private final String url;
    private LocalDateTime lastFailure;
    private final Queue<Long> recentResponseTimes;

    private final AtomicBoolean healthy = new AtomicBoolean(true);
    private final AtomicInteger errors = new AtomicInteger(0);

    public Server(String url) {
        this.url = url;
        this.recentResponseTimes = new ConcurrentLinkedQueue<>();
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
        this.errors.set(0);
        this.recentResponseTimes.clear();
    }

    public void addResponseTime(long responseTime) {
        recentResponseTimes.offer(responseTime);
        while (recentResponseTimes.size() > RESPONSE_HISTORY_SIZE) {
            recentResponseTimes.poll();
        }
    }

    public void incrementErrors() {
        errors.incrementAndGet();
    }

    public int getConsecutiveErrors() {
        return errors.get();
    }

    public Queue<Long> getRecentResponseTimes() {
        return recentResponseTimes;
    }
}


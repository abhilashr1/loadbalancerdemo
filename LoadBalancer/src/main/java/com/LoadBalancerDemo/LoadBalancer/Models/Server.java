package com.LoadBalancerDemo.LoadBalancer.Models;

import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int RESPONSE_HISTORY_SIZE = 10;

    private final String url;

    private LocalDateTime lastFailureFromError;
    private LocalDateTime lastFailureFromSlowResponse;
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

    public void setHealthy() {
        if (!this.healthy.get()) {
            logger.info("Server {} back to healthy", url);
        }
        this.healthy.set(true);
        this.lastFailureFromError = null;
        this.lastFailureFromSlowResponse = null;
        this.recentResponseTimes.clear();
        this.errors.set(0);
    }

    public void setUnhealthyFromError() {
        if (this.healthy.get()) {
            logger.warn("Server {} down from errors", url);
        }
        lastFailureFromError = LocalDateTime.now();
        this.healthy.set(false);
    }

    public void setUnhealthyFromSlowResponse() {
        if (this.healthy.get()) {
            logger.warn("Server {} down from slow responses", url);
        }
        lastFailureFromSlowResponse = LocalDateTime.now();
        this.healthy.set(false);
        this.recentResponseTimes.clear();
    }

    public LocalDateTime getLastFailureFromError() {
        return lastFailureFromError;
    }

    public LocalDateTime getLastFailureFromSlowResponse() {
        return lastFailureFromSlowResponse;
    }

    public void resetFailCount() {
        if (errors.get() > 0) {
            logger.info("Resetting error count for server {}", url);
        }
        this.errors.set(0);
        this.lastFailureFromError = null;
        if (lastFailureFromSlowResponse == null) {
            this.healthy.set(true);
        }
    }

    public void addResponseTime(long responseTime) {
        recentResponseTimes.offer(responseTime);
        while (recentResponseTimes.size() > RESPONSE_HISTORY_SIZE) {
            recentResponseTimes.poll();
        }
    }

    public void incrementErrors() {
        int currentErrors = errors.incrementAndGet();
        logger.warn("Error count increased to {} for server {}", currentErrors, url);
    }

    public int getErrors() {
        return errors.get();
    }

    public Queue<Long> getRecentResponseTimes() {
        return recentResponseTimes;
    }
}


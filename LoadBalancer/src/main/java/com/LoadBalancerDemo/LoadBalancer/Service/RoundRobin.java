package com.LoadBalancerDemo.LoadBalancer.Service;

import com.LoadBalancerDemo.LoadBalancer.Models.Server;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class RoundRobin {
    private final List<Server> servers;
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final RestTemplate restTemplate;

    private final Counter requestCounter;
    private final Counter errorCounter;
    private final Map<String, Counter> serverRequestCounters;

    public RoundRobin(RestTemplate restTemplate, MeterRegistry registry) {
        this.restTemplate = restTemplate;

        this.servers = new CopyOnWriteArrayList<>(List.of(
                new Server("http://localhost:8081"),
                new Server("http://localhost:8082"),
                new Server("http://localhost:8083")
        ));

        this.requestCounter = Counter.builder("loadbalancer.requests.total")
                .description("Total number of requests processed")
                .register(registry);

        this.errorCounter = Counter.builder("loadbalancer.requests.errors")
                .description("Total number of failed requests")
                .register(registry);

        this.serverRequestCounters = servers.stream()
                .collect(Collectors.toMap(
                        Server::getUrl,
                        server -> Counter.builder("loadbalancer.server.requests")
                                .tag("server", server.getUrl())
                                .description("Requests per server")
                                .register(registry)
                ));
    }

    public ResponseEntity<String> forwardRequest(byte[] body, HttpServletRequest request) {
        String path = request.getRequestURI();
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        Server server = getNextHealthyServer();
        requestCounter.increment();
        serverRequestCounters.get(server.getUrl()).increment();

        String url = server.getUrl() + path;

        try {
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames != null && headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                headers.add(headerName, headerValue);
            }

            HttpEntity<String> requestEntity = new HttpEntity<>(
                    body != null ? new String(body) : "",
                    headers
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    method,
                    requestEntity,
                    String.class
            );

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.putAll(response.getHeaders());
            responseHeaders.remove(HttpHeaders.TRANSFER_ENCODING);

            String responseBody = response.getBody();
            if (responseBody != null) {
                responseHeaders.setContentLength(responseBody.length());
            }

            server.resetFailCount();
            return ResponseEntity
                    .status(response.getStatusCode())
                    .headers(responseHeaders)
                    .body(responseBody);

        } catch (Exception e) {
            errorCounter.increment();
            handleServerFailure(server);
            throw e;
        }
    }

    private synchronized Server getNextHealthyServer() {
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

    private void handleServerFailure(Server server) {
        server.setHealthy(false);
    }
}
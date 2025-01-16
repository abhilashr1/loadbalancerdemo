package com.LoadBalancerDemo.LoadBalancer.Service;

import com.LoadBalancerDemo.LoadBalancer.Models.Server;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoundRobin {
    private final List<Server> servers;
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final WebClient webClient;
    private final Counter requestCounter;
    private final Counter errorCounter;
    private final Map<String, Counter> serverRequestCounters;

    public RoundRobin(WebClient webClient, MeterRegistry registry) {
        this.webClient = webClient;

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

        // Initialize per-server counters
        this.serverRequestCounters = servers.stream()
                .collect(Collectors.toMap(
                    Server::getUrl,
                    server -> Counter.builder("loadbalancer.server.requests")
                            .tag("server", server.getUrl())
                            .description("Requests per server")
                            .register(registry)
                ));
    }

    public Mono<ResponseEntity<String>> forwardRequest(byte[] body, HttpServletRequest request) {
        String path = request.getRequestURI();
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        Server server = getNextHealthyServer();
        requestCounter.increment();
        serverRequestCounters.get(server.getUrl()).increment();

        String url = server.getUrl() + path;
        byte[] safeBody = body == null ? new byte[0] : body;

        return webClient.method(method)
                .uri(url)
                // add headers???
                .bodyValue(safeBody)
                .exchangeToMono(clientResponse -> clientResponse
                        .toEntity(String.class)
                )
                .doOnSuccess(response -> {
                    server.resetFailCount();
                })
                .doOnError(error -> {
                    errorCounter.increment();
                    handleServerFailure(server);
                    forwardRequest(safeBody, request).subscribe();
                });
    }

    private Server getNextHealthyServer() {
        int attempts = 0;
        int maxAttempts = servers.size();

        while (attempts < maxAttempts) {
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

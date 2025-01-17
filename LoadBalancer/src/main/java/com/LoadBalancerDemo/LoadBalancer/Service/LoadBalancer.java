package com.LoadBalancerDemo.LoadBalancer.Service;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.LoadBalancerDemo.LoadBalancer.Factory.ServerFactory;
import com.LoadBalancerDemo.LoadBalancer.Models.Server;
import com.LoadBalancerDemo.LoadBalancer.Service.Strategies.ILoadBalancingStrategy;

@Service
public class LoadBalancer {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private final List<Server> servers;
    private final RestTemplate restTemplate;
    private final ILoadBalancingStrategy strategy;

    private Counter requestCounter;
    private Counter errorCounter;
    private Map<String, Counter> serverRequestCounters;

    public LoadBalancer(RestTemplate restTemplate,
                        ILoadBalancingStrategy lbStrategy,
                        ServerFactory serverFactory,
                        // add Request and response Builder here
                        MeterRegistry registry) {
        this.restTemplate = restTemplate;
        this.strategy = lbStrategy;
        this.servers = serverFactory.initializeServers();

        initializeCounters(registry);
    }

    public CompletableFuture<ResponseEntity<String>> forwardRequest(byte[] body, HttpServletRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String path = request.getRequestURI();
            HttpMethod method = HttpMethod.valueOf(request.getMethod());

            Server server = this.strategy.getNextServer(this.servers);
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
                this.strategy.handleServerFailure(server);
                throw e;
            }
        }, executor);
    }

    private void initializeCounters(MeterRegistry registry) {
        this.requestCounter = Counter.builder("loadbalancer.requests.total")
            .description("Total number of requests processed")
            .register(registry);

        this.errorCounter = Counter.builder("loadbalancer.requests.errors")
            .description("Total number of failed requests")
            .register(registry);

        this.serverRequestCounters = servers
            .stream()
            .collect(Collectors.toMap(
                Server::getUrl,
                server -> Counter.builder("loadbalancer.server.requests")
                        .tag("server", server.getUrl())
                        .description("Requests per server")
                        .register(registry)
            ));
    }
}
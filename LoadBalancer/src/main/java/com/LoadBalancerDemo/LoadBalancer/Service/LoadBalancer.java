package com.LoadBalancerDemo.LoadBalancer.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.LoadBalancerDemo.LoadBalancer.Factory.ServerFactory;
import com.LoadBalancerDemo.LoadBalancer.Factory.StrategyFactory;
import com.LoadBalancerDemo.LoadBalancer.Models.Server;
import com.LoadBalancerDemo.LoadBalancer.Service.BalancingStrategies.ILoadBalancingStrategy;
import com.LoadBalancerDemo.LoadBalancer.Builder.RequestBuilder;
import com.LoadBalancerDemo.LoadBalancer.Builder.ResponseBuilder;

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
                        StrategyFactory strategyFactory,
                        ServerFactory serverFactory,
                        MeterRegistry registry) {
        this.restTemplate = restTemplate;
        this.strategy = strategyFactory.createStrategy();
        this.servers = serverFactory.initializeServers();

        initializeCounters(registry);
    }

    public CompletableFuture<ResponseEntity<String>> forwardRequest(byte[] body, HttpServletRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            Server server = this.strategy.getNextServer(this.servers);
            requestCounter.increment();
            serverRequestCounters.get(server.getUrl()).increment();

            RequestBuilder requestBuilder = RequestBuilder.from(request, body);
            String url = server.getUrl() + requestBuilder.getPath();
            
            long startTime = System.currentTimeMillis();
            
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.valueOf(requestBuilder.getMethod()),
                    requestBuilder.copyHeaders().build(),
                    String.class
                );

                long responseTime = System.currentTimeMillis() - startTime;
                server.addResponseTime(responseTime);

                ResponseEntity<String> finalResponse = ResponseBuilder.from(response)
                    .copyHeaders()
                    .setContentLength()
                    .build();

                server.resetFailCount();
                return finalResponse;

            } catch (Exception e) {
                errorCounter.increment();
                server.incrementErrors();
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
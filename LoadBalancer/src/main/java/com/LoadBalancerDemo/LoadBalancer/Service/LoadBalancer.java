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

import com.LoadBalancerDemo.LoadBalancer.Factory.StrategyFactory;
import com.LoadBalancerDemo.LoadBalancer.Models.Server;
import com.LoadBalancerDemo.LoadBalancer.Service.BalancingStrategies.ILoadBalancingStrategy;
import com.LoadBalancerDemo.LoadBalancer.Builder.RequestBuilder;
import com.LoadBalancerDemo.LoadBalancer.Builder.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LoadBalancer {
    private static final Logger logger = LoggerFactory.getLogger(LoadBalancer.class);
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private final List<Server> servers;
    private final RestTemplate restTemplate;
    private final ILoadBalancingStrategy strategy;

    private Counter requestCounter;
    private Counter errorCounter;
    private Map<String, Counter> serverRequestCounters;

    public LoadBalancer(RestTemplate restTemplate,
                        StrategyFactory strategyFactory,
                        List<Server> servers,
                        MeterRegistry registry) {
        this.restTemplate = restTemplate;
        this.strategy = strategyFactory.createStrategy();
        this.servers = servers;

        initializeCounters(registry);
        logger.info("LoadBalancer initialized with {} servers", servers.size());
    }

    public CompletableFuture<ResponseEntity<String>> forwardRequest(byte[] body, HttpServletRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            Server server = this.strategy.getNextServer(this.servers);
            requestCounter.increment();
            serverRequestCounters.get(server.getUrl()).increment();

            RequestBuilder requestBuilder = RequestBuilder.from(request, body);
            String url = server.getUrl() + requestBuilder.getPath();
            
            logger.info("Forwarding {} request to {}", requestBuilder.getMethod(), url);
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
                
                logger.debug("Response received from {} in {}ms", url, responseTime);

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
                logger.error("Request to {} failed: {}", url, e.getMessage(), e);
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
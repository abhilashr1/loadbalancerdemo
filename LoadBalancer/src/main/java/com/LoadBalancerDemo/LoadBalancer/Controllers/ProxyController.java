package com.LoadBalancerDemo.LoadBalancer.Controllers;

import com.LoadBalancerDemo.LoadBalancer.Service.RoundRobin;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RestController
public class ProxyController {
    private final RoundRobin loadBalancer;

    public ProxyController(RoundRobin loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @RequestMapping("/**")
    public Mono<ResponseEntity<String>> handleRequest(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return loadBalancer.forwardRequest(body, request);
    }

}
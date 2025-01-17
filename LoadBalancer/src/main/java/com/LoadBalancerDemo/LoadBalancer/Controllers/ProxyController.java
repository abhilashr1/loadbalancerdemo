package com.LoadBalancerDemo.LoadBalancer.Controllers;

import com.LoadBalancerDemo.LoadBalancer.Service.RoundRobin;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
public class ProxyController {
    private final RoundRobin loadBalancer;

    public ProxyController(RoundRobin loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @RequestMapping("/**")
    public CompletableFuture<ResponseEntity<String>> handleRequest(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return loadBalancer.forwardRequest(body, request);
    }

}
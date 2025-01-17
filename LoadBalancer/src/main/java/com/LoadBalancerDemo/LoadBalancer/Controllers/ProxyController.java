package com.LoadBalancerDemo.LoadBalancer.Controllers;

import java.util.concurrent.CompletableFuture;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.LoadBalancerDemo.LoadBalancer.Service.LoadBalancer;

@RestController
public class ProxyController {
    private final LoadBalancer loadBalancer;

    public ProxyController(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @RequestMapping("/**")
    public CompletableFuture<ResponseEntity<String>> handleRequest(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return loadBalancer.forwardRequest(body, request);
    }

}
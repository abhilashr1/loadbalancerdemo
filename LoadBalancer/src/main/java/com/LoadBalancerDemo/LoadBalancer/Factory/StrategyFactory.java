package com.LoadBalancerDemo.LoadBalancer.Factory;

import com.LoadBalancerDemo.LoadBalancer.Config.LoadBalancerProperties;
import com.LoadBalancerDemo.LoadBalancer.Service.BalancingStrategies.ILoadBalancingStrategy;
import com.LoadBalancerDemo.LoadBalancer.Service.BalancingStrategies.RoundRobinStrategy;
import org.springframework.stereotype.Component;

@Component
public class StrategyFactory {
    private final LoadBalancerProperties properties;

    public StrategyFactory(LoadBalancerProperties properties) {
        this.properties = properties;
    }

    public ILoadBalancingStrategy createStrategy() {
        return switch (properties.getStrategy().toLowerCase()) {
            case "roundrobin" -> new RoundRobinStrategy();
            default -> throw new IllegalArgumentException("Unknown strategy: " + properties.getStrategy());
        };
    }
} 
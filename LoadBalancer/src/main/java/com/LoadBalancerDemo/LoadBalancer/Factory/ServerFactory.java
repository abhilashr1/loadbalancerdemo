package com.LoadBalancerDemo.LoadBalancer.Factory;

import com.LoadBalancerDemo.LoadBalancer.Config.LoadBalancerProperties;
import com.LoadBalancerDemo.LoadBalancer.Models.Server;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ServerFactory {
    private final LoadBalancerProperties properties;

    public ServerFactory(LoadBalancerProperties properties) {
        this.properties = properties;
    }

    public List<Server> initializeServers() {
        return new CopyOnWriteArrayList<>(
            properties.getServers().stream()
                .map(this::createServer)
                .toList()
        );
    }

    public Server createServer(String url) {
        return new Server(url);
    }
}

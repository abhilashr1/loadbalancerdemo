package com.LoadBalancerDemo.LoadBalancer.Factory;

import com.LoadBalancerDemo.LoadBalancer.Models.Server;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ServerFactory {
    public List<Server> initializeServers() {
        return new CopyOnWriteArrayList<>(List.of(
                createServer("http://localhost:8081"),
                createServer("http://localhost:8082"),
                createServer("http://localhost:8083")
        ));
    }

    public Server createServer(String url) {
        return new Server(url);
    }
}

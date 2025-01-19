package com.LoadBalancerDemo.LoadBalancer.Service.BalancingStrategies;

import com.LoadBalancerDemo.LoadBalancer.Models.Server;

import java.util.List;

public interface ILoadBalancingStrategy {
    Server getNextServer(List<Server> servers);
    void handleServerFailure(Server server);
}

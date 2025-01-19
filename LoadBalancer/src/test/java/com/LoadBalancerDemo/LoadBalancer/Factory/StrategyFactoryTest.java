package com.LoadBalancerDemo.LoadBalancer.Factory;

import com.LoadBalancerDemo.LoadBalancer.Config.LoadBalancerProperties;
import com.LoadBalancerDemo.LoadBalancer.Service.BalancingStrategies.ILoadBalancingStrategy;
import com.LoadBalancerDemo.LoadBalancer.Service.BalancingStrategies.RoundRobinStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class StrategyFactoryTest {
    @Mock
    private LoadBalancerProperties properties;

    private StrategyFactory factory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        factory = new StrategyFactory(properties);
    }

    @Test
    void createStrategy_RoundRobin_Success() {
        when(properties.getStrategy()).thenReturn("roundrobin");
        
        ILoadBalancingStrategy strategy = factory.createStrategy();
        
        assertNotNull(strategy);
        assertTrue(strategy instanceof RoundRobinStrategy);
    }

    @Test
    void createStrategy_UnknownStrategy_ThrowsException() {
        when(properties.getStrategy()).thenReturn("unknown");
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> factory.createStrategy()
        );
        
        assertEquals("Unknown strategy: unknown", exception.getMessage());
    }
} 
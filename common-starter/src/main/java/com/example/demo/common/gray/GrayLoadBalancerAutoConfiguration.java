package com.example.demo.common.gray;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;

@AutoConfiguration
@LoadBalancerClients(defaultConfiguration = GrayLoadBalancerConfiguration.class)
public class GrayLoadBalancerAutoConfiguration {
}

package com.example.demo.common.gray;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/** 注意：不要加 @Configuration，它只作为每个服务的 LoadBalancer 子容器配置，避免被主容器扫描 */
public class GrayLoadBalancerConfiguration {
    @Bean
    public ReactorLoadBalancer<ServiceInstance> grayLoadBalancer(Environment env, LoadBalancerClientFactory factory) {
        String name = env.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new GrayLoadBalancer(name, factory.getLazyProvider(name, ServiceInstanceListSupplier.class));
    }
}

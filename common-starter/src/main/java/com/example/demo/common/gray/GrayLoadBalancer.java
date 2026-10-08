package com.example.demo.common.gray;

import com.example.demo.common.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 改造点：Spring Cloud LoadBalancer 的实例选择逻辑。
 * 规则：请求头 X-Version 匹配实例元数据 version；没带头的流量只走稳定版(v1 / 无 version)；
 *      目标版本没有实例时降级到全部实例，保证可用性。
 * 网关(WebFlux)与 OpenFeign(Servlet) 共用同一份实现。
 */
public class GrayLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    private static final Logger log = LoggerFactory.getLogger(GrayLoadBalancer.class);
    static final String STABLE = "v1";

    private final String serviceId;
    private final ObjectProvider<ServiceInstanceListSupplier> supplierProvider;
    private final AtomicInteger position = new AtomicInteger(ThreadLocalRandom.current().nextInt(1000));

    public GrayLoadBalancer(String serviceId, ObjectProvider<ServiceInstanceListSupplier> supplierProvider) {
        this.serviceId = serviceId;
        this.supplierProvider = supplierProvider;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = supplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next().map(list -> pick(list, request));
    }

    private Response<ServiceInstance> pick(List<ServiceInstance> all, Request<?> request) {
        if (all.isEmpty()) {
            log.warn("No servers available for service: {}", serviceId);
            return new EmptyResponse();
        }
        String want = wantedVersion(request);
        List<ServiceInstance> candidates = all.stream().filter(i -> matches(i, want)).toList();
        if (candidates.isEmpty()) {
            log.warn("No instance of {} matches version={}, fallback to all", serviceId, want);
            candidates = all;
        }
        int idx = (position.getAndIncrement() & Integer.MAX_VALUE) % candidates.size();
        return new DefaultResponse(candidates.get(idx));
    }

    private boolean matches(ServiceInstance instance, String want) {
        String v = instance.getMetadata().getOrDefault("version", STABLE);
        return want == null ? STABLE.equals(v) : want.equals(v);
    }

    private String wantedVersion(Request<?> request) {
        if (request.getContext() instanceof RequestDataContext ctx && ctx.getClientRequest() != null) {
            return ctx.getClientRequest().getHeaders().getFirst(RequestContext.H_VERSION);
        }
        return null;
    }
}

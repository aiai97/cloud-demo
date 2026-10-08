package com.example.demo.gateway;

import com.example.demo.common.RequestContext;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** 场景：灰度发布。白名单用户打上 X-Version=v2，后续由 GrayLoadBalancer + Feign 透传实现全链路灰度 */
@Component
public class GrayGlobalFilter implements GlobalFilter, Ordered {
    private final GrayProperties props;

    public GrayGlobalFilter(GrayProperties props) {
        this.props = props;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String uid = exchange.getRequest().getHeaders().getFirst(RequestContext.H_USER);
        if (props.isEnabled() && uid != null && props.getUids().contains(uid)) {
            ServerHttpRequest req = exchange.getRequest().mutate()
                    .header(RequestContext.H_VERSION, props.getVersion())
                    .build();
            return chain.filter(exchange.mutate().request(req).build());
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -90;
    }
}

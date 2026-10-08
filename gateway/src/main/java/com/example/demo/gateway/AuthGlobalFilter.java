package com.example.demo.gateway;

import com.example.demo.common.RequestContext;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 场景：统一鉴权 + 多租户识别。
 * 演示 token 格式：token-{tenantId}-{userId}，例如 token-t1-1001。
 * 解析后用 header(...) 覆盖写入，防止客户端伪造 X-Tenant-Id / X-User-Id。
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        String[] p = token == null ? new String[0] : token.split("-");
        if (p.length != 3 || !"token".equals(p[0])) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        ServerHttpRequest req = exchange.getRequest().mutate()
                .header(RequestContext.H_TENANT, p[1])
                .header(RequestContext.H_USER, p[2])
                .build();
        return chain.filter(exchange.mutate().request(req).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

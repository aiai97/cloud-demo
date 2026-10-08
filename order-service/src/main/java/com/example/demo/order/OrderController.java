package com.example.demo.order;

import com.example.demo.common.BizException;
import com.example.demo.common.RateLimit;
import com.example.demo.common.RequestContext;
import com.example.demo.common.Result;
import com.example.demo.order.client.UserClient;
import com.example.demo.order.client.UserDTO;
import com.example.demo.order.strategy.PriceStrategyFactory;
import com.example.demo.order.strategy.Scene;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final UserClient userClient;
    private final PriceStrategyFactory factory;

    @Value("${app.version:v1}")
    private String version;
    @Value("${server.port}")
    private int port;

    public OrderController(UserClient userClient, PriceStrategyFactory factory) {
        this.userClient = userClient;
        this.factory = factory;
    }

    @PostMapping("/create")
    @RateLimit(permits = 10, windowSeconds = 1)
    public OrderVO create(@RequestBody CreateOrderReq req) {
        String uid = RequestContext.get(RequestContext.H_USER);
        if (uid == null) {
            throw new BizException(401, "缺少用户信息，请通过网关访问");
        }
        // Feign 调用：X-Tenant-Id / X-User-Id / X-Version 由 FeignContextInterceptor 自动透传，
        // 目标实例由 GrayLoadBalancer 按版本选择
        Result<UserDTO> r = userClient.get(Long.valueOf(uid));
        if (!r.isOk()) {
            throw new BizException(r.code(), r.msg());
        }
        UserDTO user = r.data();

        Scene scene = req.scene() != null ? req.scene() : (user.vip() ? Scene.VIP : Scene.NORMAL);
        BigDecimal pay = factory.get(scene).calculate(req.amount());

        return new OrderVO("O" + UUID.randomUUID().toString().substring(0, 8), user.tenantId(), user.id(), scene,
                req.amount(), pay, version + "@" + port, user.servedBy());
    }
}

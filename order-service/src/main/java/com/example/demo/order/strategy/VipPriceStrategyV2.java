package com.example.demo.order.strategy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 会员价 v2：8.5 折，仅灰度实例(order.vip-strategy=v2)加载，用来验证新规则 */
@Component
@ConditionalOnProperty(name = "order.vip-strategy", havingValue = "v2")
public class VipPriceStrategyV2 implements PriceStrategy {
    public Scene scene() { return Scene.VIP; }

    public BigDecimal calculate(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.85")).setScale(2, RoundingMode.HALF_UP);
    }
}

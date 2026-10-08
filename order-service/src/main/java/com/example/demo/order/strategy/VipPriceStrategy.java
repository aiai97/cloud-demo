package com.example.demo.order.strategy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 会员价 v1：9 折（默认） */
@Component
@ConditionalOnProperty(name = "order.vip-strategy", havingValue = "v1", matchIfMissing = true)
public class VipPriceStrategy implements PriceStrategy {
    public Scene scene() { return Scene.VIP; }

    public BigDecimal calculate(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP);
    }
}

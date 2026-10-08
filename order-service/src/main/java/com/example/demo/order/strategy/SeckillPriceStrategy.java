package com.example.demo.order.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class SeckillPriceStrategy implements PriceStrategy {
    public Scene scene() { return Scene.SECKILL; }

    public BigDecimal calculate(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
    }
}

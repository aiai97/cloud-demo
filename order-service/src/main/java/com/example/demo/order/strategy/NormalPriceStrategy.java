package com.example.demo.order.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class NormalPriceStrategy implements PriceStrategy {
    public Scene scene() { return Scene.NORMAL; }

    public BigDecimal calculate(BigDecimal amount) { return amount; }
}

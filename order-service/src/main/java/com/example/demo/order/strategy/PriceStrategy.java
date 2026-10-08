package com.example.demo.order.strategy;

import java.math.BigDecimal;

/** 场景：不同业务场景(普通 / 会员 / 秒杀)使用不同计价规则 */
public interface PriceStrategy {
    Scene scene();

    BigDecimal calculate(BigDecimal amount);
}

package com.example.demo.order;

import com.example.demo.order.strategy.Scene;

import java.math.BigDecimal;

public record OrderVO(String orderNo, String tenantId, Long userId, Scene scene,
                      BigDecimal originAmount, BigDecimal payAmount,
                      String orderServedBy, String userServedBy) {
}

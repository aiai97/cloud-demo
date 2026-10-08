package com.example.demo.order;

import com.example.demo.order.strategy.Scene;

import java.math.BigDecimal;

/** scene 为空时：会员自动走 VIP，其余走 NORMAL */
public record CreateOrderReq(Long productId, BigDecimal amount, Scene scene) {
}

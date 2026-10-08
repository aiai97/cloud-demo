package com.example.demo.common;

import java.lang.annotation.*;

/** 按「租户 + 方法」维度的固定窗口限流（演示用，生产建议换 Redis / Sentinel） */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    int permits() default 10;
    int windowSeconds() default 1;
}

package com.example.demo.common.web;

import com.example.demo.common.BizException;
import com.example.demo.common.RateLimit;
import com.example.demo.common.RequestContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
public class RateLimitAspect {
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        String tenant = Objects.toString(RequestContext.get(RequestContext.H_TENANT), "default");
        String key = tenant + ":" + pjp.getSignature().toLongString();
        Window w = windows.computeIfAbsent(key, k -> new Window());
        if (!w.tryAcquire(rateLimit.permits(), rateLimit.windowSeconds() * 1000L)) {
            throw new BizException(429, "请求过于频繁，请稍后再试");
        }
        return pjp.proceed();
    }

    private static class Window {
        private long start = System.currentTimeMillis();
        private int count;

        synchronized boolean tryAcquire(int max, long windowMs) {
            long now = System.currentTimeMillis();
            if (now - start >= windowMs) {
                start = now;
                count = 0;
            }
            return ++count <= max;
        }
    }
}

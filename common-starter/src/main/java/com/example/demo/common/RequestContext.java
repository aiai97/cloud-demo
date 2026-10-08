package com.example.demo.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请求上下文：租户 / 用户 / 灰度版本。
 * 由 ContextFilter 写入，由 FeignContextInterceptor 向下游透传。
 */
public final class RequestContext {
    public static final String H_TENANT = "X-Tenant-Id";
    public static final String H_USER = "X-User-Id";
    public static final String H_VERSION = "X-Version";
    public static final List<String> PROPAGATE = List.of(H_TENANT, H_USER, H_VERSION);

    private static final ThreadLocal<Map<String, String>> HOLDER = new ThreadLocal<>();

    private RequestContext() {}

    public static void set(Map<String, String> values) {
        HOLDER.set(new HashMap<>(values));
    }

    public static String get(String key) {
        Map<String, String> m = HOLDER.get();
        return m == null ? null : m.get(key);
    }

    public static Map<String, String> all() {
        Map<String, String> m = HOLDER.get();
        return m == null ? Collections.emptyMap() : m;
    }

    public static void clear() {
        HOLDER.remove();
    }
}

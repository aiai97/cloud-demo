package com.example.demo.common.web;

import com.example.demo.common.RequestContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/** 场景：多租户 + 全链路灰度——调用下游时把上下文头原样带过去 */
public class FeignContextInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        RequestContext.all().forEach((k, v) -> {
            if (!template.headers().containsKey(k)) {
                template.header(k, v);
            }
        });
    }
}

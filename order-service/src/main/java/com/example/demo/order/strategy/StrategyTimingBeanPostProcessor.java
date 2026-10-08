package com.example.demo.order.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

/**
 * 改造点：Spring 容器的 Bean 生命周期。
 * 在 Bean 初始化之后，把所有 PriceStrategy 包一层 JDK 代理，统一打印耗时，
 * 业务策略类本身完全不用改。
 */
@Component
public class StrategyTimingBeanPostProcessor implements BeanPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(StrategyTimingBeanPostProcessor.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (!(bean instanceof PriceStrategy target)) {
            return bean;
        }
        return Proxy.newProxyInstance(bean.getClass().getClassLoader(), new Class[]{PriceStrategy.class},
                (proxy, method, args) -> {
                    long start = System.nanoTime();
                    try {
                        return method.invoke(target, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    } finally {
                        if ("calculate".equals(method.getName())) {
                            log.info("[strategy] {} calculate cost {} us", beanName, (System.nanoTime() - start) / 1000);
                        }
                    }
                });
    }
}

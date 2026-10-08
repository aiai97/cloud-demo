package com.example.demo.order.strategy;

import com.example.demo.common.BizException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PriceStrategyFactory {
    private final Map<Scene, PriceStrategy> strategies = new EnumMap<>(Scene.class);

    public PriceStrategyFactory(List<PriceStrategy> list) {
        list.forEach(s -> strategies.put(s.scene(), s));
    }

    public PriceStrategy get(Scene scene) {
        PriceStrategy s = strategies.get(scene);
        if (s == null) {
            throw new BizException(400, "不支持的业务场景: " + scene);
        }
        return s;
    }
}

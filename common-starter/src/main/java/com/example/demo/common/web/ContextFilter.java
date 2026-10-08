package com.example.demo.common.web;

import com.example.demo.common.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        Map<String, String> values = new HashMap<>();
        for (String h : RequestContext.PROPAGATE) {
            String v = req.getHeader(h);
            if (v != null) {
                values.put(h, v);
            }
        }
        RequestContext.set(values);
        try {
            chain.doFilter(req, resp);
        } finally {
            RequestContext.clear();
        }
    }
}

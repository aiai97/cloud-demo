package com.example.demo.user;

import com.example.demo.common.BizException;
import com.example.demo.common.RateLimit;
import com.example.demo.common.RequestContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @Value("${app.version:v1}")
    private String version;
    @Value("${server.port}")
    private int port;

    /** 直接返回业务对象，由 starter 里的 ResultWrapAdvice 统一包装为 Result */
    @GetMapping("/{id}")
    @RateLimit(permits = 20, windowSeconds = 1)
    public UserDTO get(@PathVariable Long id) {
        if (id >= 9000) {
            throw new BizException(404, "用户不存在: " + id);
        }
        return new UserDTO(id, "user-" + id, id % 2 == 1, version + "@" + port,
                RequestContext.get(RequestContext.H_TENANT));
    }
}

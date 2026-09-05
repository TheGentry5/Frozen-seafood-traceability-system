package com.example.frozen_seafood_traceability_system.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.frozen_seafood_traceability_system.common.AuthContext;
import com.example.frozen_seafood_traceability_system.common.BizCode;
import com.example.frozen_seafood_traceability_system.common.Principal;
import com.example.frozen_seafood_traceability_system.common.TokenStore;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 登录校验：校验 X-Token 是否在 TokenStore 中，命中后写入 AuthContext。
 * 白名单（登录 / 消费者溯源）在 WebMvcConfig 中排除，不进入本拦截器。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String token = request.getHeader("X-Token");
        Principal principal = tokenStore.get(token);
        if (principal == null) {
            write401(response);
            return false;
        }
        AuthContext.setToken(token);
        AuthContext.set(principal);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private void write401(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=" + StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
                com.example.frozen_seafood_traceability_system.common.Result
                        .fail(BizCode.UNAUTHORIZED, "未登录或登录已失效")));
    }
}

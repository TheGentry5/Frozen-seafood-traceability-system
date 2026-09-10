package com.example.frozen_seafood_traceability_system.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.frozen_seafood_traceability_system.common.AuthContext;
import com.example.frozen_seafood_traceability_system.common.BizCode;
import com.example.frozen_seafood_traceability_system.common.Principal;
import com.example.frozen_seafood_traceability_system.common.StatusConst;
import com.example.frozen_seafood_traceability_system.common.TokenStore;
import com.example.frozen_seafood_traceability_system.entity.NodeInfo;
import com.example.frozen_seafood_traceability_system.mapper.NodeInfoMapper;
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
    private NodeInfoMapper nodeInfoMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 跨域预检不带自定义头，直接放行，避免被 401
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }
        String token = request.getHeader("X-Token");
        Principal principal = tokenStore.get(token);
        if (principal == null) {
            writeUnauthorized(response, "未登录或登录已失效");
            return false;
        }
        // 节点企业每次请求重查状态，停用/改类型后已发 token 立即失效
        if ("NODE".equals(principal.getType())) {
            NodeInfo node = nodeInfoMapper.selectById(principal.getId());
            if (node == null || !Objects.equals(node.getStatus(), StatusConst.NODE_ENABLED)) {
                tokenStore.remove(token);
                writeUnauthorized(response, "企业已被停用，请重新登录");
                return false;
            }
            principal.setNodeType(node.getNodeType());
            principal.setName(node.getNodeName());
        }
        AuthContext.setToken(token);
        AuthContext.set(principal);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=" + StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
                com.example.frozen_seafood_traceability_system.common.Result
                        .fail(BizCode.UNAUTHORIZED, msg)));
    }
}

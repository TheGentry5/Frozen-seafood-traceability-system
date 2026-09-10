package com.example.frozen_seafood_traceability_system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * HTTP 契约：未登录 401、未知路径 404、错误方法 400。
 * 走真实内嵌服务器，验证 WebMvc 配置与全局异常处理（无写操作，故不需事务回滚）。
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class HttpContractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode exchange(HttpMethod method, String url) throws Exception {
        ResponseEntity<String> resp = restTemplate.exchange(url, method, null, String.class);
        return objectMapper.readTree(resp.getBody());
    }

    @Test
    @DisplayName("无 token 访问受保护接口返回 code=401")
    void noToken() throws Exception {
        org.assertj.core.api.Assertions.assertThat(exchange(HttpMethod.GET, "/api/farm/batch").path("code").asInt())
                .isEqualTo(401);
    }

    @Test
    @DisplayName("未知路径返回 code=404（统一 Result 封装）")
    void notFound() throws Exception {
        org.assertj.core.api.Assertions.assertThat(exchange(HttpMethod.GET, "/api/nope/nope").path("code").asInt())
                .isEqualTo(404);
    }

    @Test
    @DisplayName("错误 HTTP 方法返回 code=400")
    void methodNotAllowed() throws Exception {
        org.assertj.core.api.Assertions.assertThat(exchange(HttpMethod.GET, "/api/login").path("code").asInt())
                .isEqualTo(400);
    }
}

package com.example.frozen_seafood_traceability_system;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 登录、鉴权与统一异常契约。
 */
class AuthIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("节点登录成功返回 token 与用户信息")
    void nodeLoginOk() throws Exception {
        JsonNode r = call(HttpMethod.POST, "/api/login", null,
                Map.of("nodeCode", "FARM001", "password", DEFAULT_PASSWORD));
        assertCode(r, 200, "节点登录");
        org.assertj.core.api.Assertions.assertThat(r.get("data").get("token").asText()).isNotBlank();
        org.assertj.core.api.Assertions.assertThat(r.get("data").get("user").get("nodeType").asInt()).isEqualTo(1);
    }

    @Test
    @DisplayName("密码错误返回 400")
    void wrongPassword() throws Exception {
        JsonNode r = call(HttpMethod.POST, "/api/login", null,
                Map.of("nodeCode", "FARM001", "password", "bad-password"));
        assertCode(r, 400, "密码错误");
    }

    @Test
    @DisplayName("停用企业不能登录")
    void disabledNodeCannotLogin() throws Exception {
        setNodeStatus(1L, 2);
        JsonNode r = call(HttpMethod.POST, "/api/login", null,
                Map.of("nodeCode", "FARM001", "password", DEFAULT_PASSWORD));
        assertCode(r, 400, "停用企业登录");
    }

    @Test
    @DisplayName("无 token 访问受保护接口返回 401")
    void noTokenUnauthorized() throws Exception {
        assertCode(call(HttpMethod.GET, "/api/farm/batch", null, null), 401, "无 token");
    }

    @Test
    @DisplayName("角色不符返回 403（管理员访问节点接口 / 节点访问管理端）")
    void crossRoleForbidden() throws Exception {
        String admin = loginAdmin();
        String farm = loginNode("FARM001");
        assertCode(call(HttpMethod.GET, "/api/farm/batch", admin, null), 403, "管理员访问节点接口");
        assertCode(call(HttpMethod.GET, "/api/admin/node", farm, null), 403, "节点访问管理端");
    }

    @Test
    @DisplayName("修改密码后原 token 立即失效")
    void updatePasswordRevokesToken() throws Exception {
        String farm = loginNode("FARM001");
        JsonNode r = call(HttpMethod.POST, "/api/user/updatePwd", farm,
                Map.of("oldPwd", DEFAULT_PASSWORD, "newPwd", "newPass123"));
        assertCode(r, 200, "修改密码");
        assertCode(call(HttpMethod.GET, "/api/farm/batch", farm, null), 401, "旧 token 失效");
    }

    @Test
    @DisplayName("停用企业后已发 token 立即失效（拦截器每请求重查状态）")
    void disabledNodeTokenRevoked() throws Exception {
        String farm = loginNode("FARM001");
        assertCode(call(HttpMethod.GET, "/api/farm/batch", farm, null), 200, "停用前可访问");
        setNodeStatus(1L, 2);
        assertCode(call(HttpMethod.GET, "/api/farm/batch", farm, null), 401, "停用后 token 失效");
    }
}

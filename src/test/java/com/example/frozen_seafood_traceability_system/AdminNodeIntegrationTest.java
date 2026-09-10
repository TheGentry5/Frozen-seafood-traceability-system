package com.example.frozen_seafood_traceability_system;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 管理端企业注册管理：CRUD、区域校验、删除/改类型保护、统计。
 */
class AdminNodeIntegrationTest extends IntegrationTestBase {

    private Map<String, Object> body(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }

    private long findNodeId(String admin, String keyword) throws Exception {
        JsonNode r = call(HttpMethod.GET, "/api/admin/node?nodeName=" + keyword, admin, null);
        assertCode(r, 200, "企业查询");
        return r.get("data").get("list").get(0).get("id").asLong();
    }

    @Test
    @DisplayName("管理端企业 新建 / 查询 / 修改 / 删除")
    void nodeCrud() throws Exception {
        String admin = loginAdmin();
        assertCode(call(HttpMethod.POST, "/api/admin/node", admin,
                body("nodeCode", "NEWRETA", "nodeName", "新零售", "nodeType", 4,
                        "provinceCode", "350000", "cityCode", "350100", "phone", "13800000000")),
                200, "新建企业");
        long id = findNodeId(admin, "新零售");
        assertCode(call(HttpMethod.PUT, "/api/admin/node/" + id, admin,
                body("nodeName", "新零售改")), 200, "修改企业");
        assertCode(call(HttpMethod.DELETE, "/api/admin/node/" + id, admin, null), 200, "删除企业");
    }

    @Test
    @DisplayName("不存在的省份返回 400")
    void invalidArea() throws Exception {
        String admin = loginAdmin();
        assertCode(call(HttpMethod.POST, "/api/admin/node", admin,
                body("nodeCode", "BADAREA", "nodeName", "错误区域", "nodeType", 1,
                        "provinceCode", "999999")),
                400, "省份不存在");
    }

    @Test
    @DisplayName("企业名下存在未下架批号时：删除 403、变更类型 400")
    void protectWhenNodeHasBatches() throws Exception {
        String admin = loginAdmin();
        String farm = loginNode("FARM001");
        assertCode(call(HttpMethod.POST, "/api/farm/batch", farm,
                body("batchCode", "T-PROTECT-1", "productName", "对虾")), 200, "养殖新建");

        assertCode(call(HttpMethod.DELETE, "/api/admin/node/1", admin, null), 403, "有批号禁止删除");
        assertCode(call(HttpMethod.PUT, "/api/admin/node/1", admin,
                body("nodeType", 2)), 400, "有批号禁止改类型");
    }

    @Test
    @DisplayName("统计接口返回 12 个月趋势、省份分布、类型分布")
    void stats() throws Exception {
        String admin = loginAdmin();
        JsonNode r = call(HttpMethod.GET, "/api/admin/stats", admin, null);
        assertCode(r, 200, "统计");
        org.assertj.core.api.Assertions.assertThat(r.get("data").get("monthTrend").size()).isEqualTo(12);
        org.assertj.core.api.Assertions.assertThat(r.get("data").get("typeDist").size()).isGreaterThan(0);
    }
}

package com.example.frozen_seafood_traceability_system;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 四环节批号流转 + 溯源码生成 + 消费者溯源 + 冷链温度 的全链路集成测试。
 */
class BatchFlowIntegrationTest extends IntegrationTestBase {

    private Map<String, Object> body(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }

    @Test
    @DisplayName("养殖→加工→批发→零售 全链流转，确认后生成溯源码并可被消费者查询")
    void fullChainAndConsumerTrace() throws Exception {
        String farm = loginNode("FARM001");
        String proc = loginNode("PROC001");
        String whol = loginNode("WHOL001");
        String reta = loginNode("RETA001");

        // 养殖：新建 + 发布
        assertCode(call(HttpMethod.POST, "/api/farm/batch", farm,
                body("batchCode", "T-FARM-1", "productName", "南美白对虾", "inspectionCert", "闽检字", "inspector", "林海")),
                200, "养殖新建");
        long fbId = findBatchId(farm, "farm", "T-FARM-1");
        assertCode(call(HttpMethod.PUT, "/api/farm/batch/" + fbId, farm,
                body("productName", "南美白对虾", "publish", true)), 200, "养殖发布");

        // 加工：引用养殖批号 → 发送确认
        assertCode(call(HttpMethod.POST, "/api/proc/batch", proc,
                body("batchCode", "T-PROC-1", "productName", "冷冻带鱼段", "productType", "中段",
                        "inNodeId", 1, "inBatchId", fbId)),
                200, "加工新建");
        long pbId = findBatchId(proc, "proc", "T-PROC-1");
        JsonNode procDetail = call(HttpMethod.GET, "/api/proc/batch/" + pbId, proc, null);
        assertCode(procDetail, 200, "加工详情");
        org.assertj.core.api.Assertions.assertThat(procDetail.get("data").get("inBatchCode").asText())
                .isEqualTo("T-FARM-1");
        assertCode(call(HttpMethod.PUT, "/api/proc/batch/" + pbId, proc,
                body("productName", "冷冻带鱼段", "sendConfirm", true)), 200, "加工发送确认");

        // 养殖确认加工进场
        assertCode(call(HttpMethod.PUT, "/api/farm/confirm/" + pbId, farm, null), 200, "养殖确认");

        // 批发：引用加工批号 → 发送确认
        assertCode(call(HttpMethod.POST, "/api/whol/batch", whol,
                body("batchCode", "T-WHOL-1", "productName", "批发鲜品", "inNodeId", 2, "inBatchId", pbId)),
                200, "批发新建");
        long wbId = findBatchId(whol, "whol", "T-WHOL-1");
        assertCode(call(HttpMethod.PUT, "/api/whol/batch/" + wbId, whol,
                body("productName", "批发鲜品", "sendConfirm", true)), 200, "批发发送确认");
        assertCode(call(HttpMethod.PUT, "/api/proc/confirm/" + wbId, proc, null), 200, "加工确认");

        // 零售：引用批发批号 → 发送确认 → 批发确认生成溯源码
        assertCode(call(HttpMethod.POST, "/api/reta/batch", reta,
                body("batchCode", "T-RETA-1", "productName", "零售鲜品", "inNodeId", 3, "inBatchId", wbId)),
                200, "零售新建");
        long rbId = findBatchId(reta, "reta", "T-RETA-1");
        assertCode(call(HttpMethod.PUT, "/api/reta/batch/" + rbId, reta,
                body("productName", "零售鲜品", "sendConfirm", true)), 200, "零售发送确认");

        JsonNode confirm = call(HttpMethod.PUT, "/api/whol/confirm/" + rbId, whol, null);
        assertCode(confirm, 200, "批发确认生成溯源码");
        String traceCode = confirm.get("data").asText();
        org.assertj.core.api.Assertions.assertThat(traceCode).startsWith("TSF-");

        // 消费者溯源：4 环节链
        JsonNode info = call(HttpMethod.GET, "/api/trace/info/" + traceCode, null, null);
        assertCode(info, 200, "消费者溯源");
        org.assertj.core.api.Assertions.assertThat(info.get("data").get("chain").size()).isEqualTo(4);

        // 温度曲线接口可访问
        assertCode(call(HttpMethod.GET, "/api/trace/info/" + traceCode + "/temperature", null, null),
                200, "消费者温度曲线");

        // 被下游引用的养殖批号不可下架
        assertCode(call(HttpMethod.PUT, "/api/farm/batch/" + fbId + "/off", farm, null), 403, "引用中下架");
    }

    @Test
    @DisplayName("同一企业内批号重复返回 409；产品名称为空返回 400")
    void uniquenessAndValidation() throws Exception {
        String farm = loginNode("FARM001");
        assertCode(call(HttpMethod.POST, "/api/farm/batch", farm,
                body("batchCode", "T-DUP-1", "productName", "对虾")), 200, "首次新建");
        assertCode(call(HttpMethod.POST, "/api/farm/batch", farm,
                body("batchCode", "T-DUP-1", "productName", "对虾")), 409, "重复批号");
        assertCode(call(HttpMethod.POST, "/api/farm/batch", farm,
                body("batchCode", "T-NONAME-1", "productName", "  ")), 400, "产品名称为空");
    }

    @Test
    @DisplayName("冷链填报：合法范围成功、越界 400、曲线返回点位")
    void coldChainReportAndCurve() throws Exception {
        String farm = loginNode("FARM001");
        assertCode(call(HttpMethod.POST, "/api/farm/batch", farm,
                body("batchCode", "T-COLD-1", "productName", "对虾")), 200, "新建");
        long fbId = findBatchId(farm, "farm", "T-COLD-1");

        assertCode(call(HttpMethod.POST, "/api/cold-chain/report", farm,
                body("batchType", 1, "batchId", fbId, "temperature", -18.5, "humidity", 80, "remark", "入库")),
                200, "填报温度");
        JsonNode curve = call(HttpMethod.GET, "/api/cold-chain/curve?batchType=1&batchId=" + fbId, farm, null);
        assertCode(curve, 200, "温度曲线");
        org.assertj.core.api.Assertions.assertThat(curve.get("data").get("points").size()).isEqualTo(1);

        assertCode(call(HttpMethod.POST, "/api/cold-chain/report", farm,
                body("batchType", 1, "batchId", fbId, "temperature", 5)), 400, "温度越界");
        assertCode(call(HttpMethod.POST, "/api/cold-chain/report", farm,
                body("batchType", 1, "batchId", fbId, "temperature", -10, "remark", "x".repeat(300))),
                400, "备注超长");
    }
}

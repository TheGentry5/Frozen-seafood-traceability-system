package com.example.frozen_seafood_traceability_system;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 集成测试基类：启动完整 Spring 上下文 + MockMvc，覆盖拦截器、参数解析、全局异常与真实 SQL。
 * 每个测试方法在事务中执行并回滚，测试前重建确定性的最小种子数据，因此可重复运行、不污染数据库。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
public abstract class IntegrationTestBase {

    protected static final String DEFAULT_PASSWORD = "123456";
    /** md5("123456") */
    protected static final String MD5_123456 = "e10adc3949ba59abbe56e057f20f883e";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected com.example.frozen_seafood_traceability_system.mapper.NodeInfoMapper nodeInfoMapper;

    // 每个用例前清空业务表并写入固定种子：admin + 四类企业（id 1..4）
    @org.junit.jupiter.api.BeforeEach
    void seedBaseData() {
        for (String table : new String[] {
                "cold_chain_record", "reta_batch", "whol_batch", "proc_batch",
                "farm_batch", "node_info", "admin" }) {
            jdbcTemplate.update("DELETE FROM " + table);
        }
        jdbcTemplate.update("INSERT IGNORE INTO province (province_code, province_name) VALUES ('350000','福建省')");
        jdbcTemplate.update("INSERT IGNORE INTO city (city_code, city_name, province_code) VALUES ('350100','福州市','350000')");
        jdbcTemplate.update("INSERT INTO admin (id, username, password) VALUES (1, 'admin', ?)", MD5_123456);
        insertNode(1L, "FARM001", "测试养殖企业", 1);
        insertNode(2L, "PROC001", "测试加工企业", 2);
        insertNode(3L, "WHOL001", "测试批发商", 3);
        insertNode(4L, "RETA001", "测试零售商", 4);
    }

    private void insertNode(Long id, String code, String name, int type) {
        jdbcTemplate.update(
                "INSERT INTO node_info (id,node_code,password,node_name,node_type,province_code,city_code,status) "
                        + "VALUES (?,?,?,?,?,'350000','350100',1)",
                id, code, MD5_123456, name, type);
    }

    /** 通过 MyBatis 更新企业状态（会失效一级缓存，避免与拦截器读库产生测试内假象） */
    protected void setNodeStatus(Long id, int status) {
        com.example.frozen_seafood_traceability_system.entity.NodeInfo patch =
                new com.example.frozen_seafood_traceability_system.entity.NodeInfo();
        patch.setId(id);
        patch.setStatus(status);
        nodeInfoMapper.updateById(patch);
    }

    /** 发起请求并解析统一响应体 {code,msg,data} */
    protected JsonNode call(HttpMethod method, String url, String token, Object body) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.request(method, url);
        if (token != null) {
            builder.header("X-Token", token);
        }
        if (body != null) {
            builder.contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body));
        }
        String content = mockMvc.perform(builder)
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return content.isEmpty() ? objectMapper.createObjectNode() : objectMapper.readTree(content);
    }

    protected String loginNode(String nodeCode) throws Exception {
        JsonNode r = call(HttpMethod.POST, "/api/login", null,
                Map.of("nodeCode", nodeCode, "password", DEFAULT_PASSWORD));
        assertCode(r, 200, "节点登录 " + nodeCode);
        return r.get("data").get("token").asText();
    }

    protected String loginAdmin() throws Exception {
        JsonNode r = call(HttpMethod.POST, "/api/admin/login", null,
                Map.of("username", "admin", "password", DEFAULT_PASSWORD));
        assertCode(r, 200, "管理员登录");
        return r.get("data").get("token").asText();
    }

    /** 按关键字在列表接口中定位批号 id */
    protected long findBatchId(String token, String module, String batchCode) throws Exception {
        JsonNode r = call(HttpMethod.GET, "/api/" + module + "/batch?keyword=" + batchCode, token, null);
        assertCode(r, 200, module + " 列表查询");
        for (JsonNode row : r.get("data").get("list")) {
            if (batchCode.equals(row.get("batchCode").asText())) {
                return row.get("id").asLong();
            }
        }
        throw new AssertionError("未找到批号 " + batchCode);
    }

    protected void assertCode(JsonNode r, int expected, String label) {
        org.assertj.core.api.Assertions.assertThat(r.path("code").asInt())
                .as("%s 期望 code=%d, 实际响应=%s", label, expected, r)
                .isEqualTo(expected);
    }
}

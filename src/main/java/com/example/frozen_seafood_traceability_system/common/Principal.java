package com.example.frozen_seafood_traceability_system.common;

/**
 * 当前登录主体：登录成功后在 TokenStore 中保存、经拦截器写入 AuthContext。
 */
public class Principal {

    /** NODE：节点企业账号；ADMIN：系统管理员 */
    private String type;
    /** 主键 id（node_info.id 或 admin.id） */
    private Long id;
    /** 登录编码 */
    private String code;
    /** 企业 node_type（仅 type=NODE 时有意义） */
    private Integer nodeType;
    /** 企业名称 */
    private String name;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getNodeType() {
        return nodeType;
    }

    public void setNodeType(Integer nodeType) {
        this.nodeType = nodeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

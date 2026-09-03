package com.example.frozen_seafood_traceability_system.service;

import java.util.Map;

import com.example.frozen_seafood_traceability_system.common.PageResult;
import com.example.frozen_seafood_traceability_system.entity.NodeInfo;

/**
 * 系统管理端接口（docs/开发实施文档.md §6.5）。
 */
public interface AdminService {

    PageResult<NodeInfo> pageNode(long page, long size,
                                  String nodeName, Integer nodeType,
                                  String provinceCode, String cityCode);

    NodeInfo detailNode(Long id);

    /** 新建注册企业（node_code 重复→409；password 缺省 123456） */
    void createNode(NodeInfo req);

    /** 编辑（不含密码） */
    void updateNode(NodeInfo req);

    /** 删除（名下存在未下架批号 → 403） */
    void deleteNode(Long id);

    /** 统计三件套：monthTrend 12 条、provinceDist、typeDist */
    Map<String, Object> stats();
}

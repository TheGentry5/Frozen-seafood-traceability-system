package com.example.frozen_seafood_traceability_system.common;

/**
 * 状态 / 企业类型常量（docs/开发实施文档.md §4.4，与建表注释一致）。
 */
public interface StatusConst {

    // ===== node_info.node_type 企业类型 =====
    int NODE_FARM = 1;     // 养殖企业
    int NODE_PROC = 2;     // 加工企业
    int NODE_WHOL = 3;     // 批发商
    int NODE_RETA = 4;     // 零售商

    // ===== node_info.status 企业状态 =====
    int NODE_ENABLED = 1;
    int NODE_DISABLED = 2;

    // ===== farm_batch.status =====
    int FARM_WAIT_RELEASE = 1;   // 待发布
    int FARM_RELEASED = 2;       // 已发布
    int FARM_OFF = 3;            // 已下架

    // ===== proc_batch / whol_batch / reta_batch.status =====
    int BATCH_NEW = 1;           // 新建
    int BATCH_WAIT_CONFIRM = 2;  // 待确认
    int BATCH_CONFIRMED = 3;     // 已确认
    int BATCH_OFF = 4;           // 已下架
}

package com.example.frozen_seafood_traceability_system.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 加工企业产品批号表 proc_batch（冷冻海产品）。
 * status: 1新建 2待确认 3已确认 4已下架
 */
@Data
@TableName("proc_batch")
public class ProcBatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 本批号所属加工企业 id */
    private Long nodeId;

    private String batchCode;

    /** 本批冷冻海产品名称 */
    private String productName;

    /** 产品类型（整条/去头/中段...） */
    private String productType;

    private String inspectionCert;

    /** 官方检验员名称 */
    private String inspector;

    /** 进场：上游养殖企业 id */
    private Long inNodeId;

    /** 进场：上游企业所在区域（省+市 名称串） */
    private String inArea;

    /** 进场：上游 farm_batch.id */
    private Long inBatchId;

    /** 进场：上游批号品种（回填展示） */
    private String inProductName;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // ---- 联表展示字段 ----
    @TableField(exist = false)
    private String nodeName;

    @TableField(exist = false)
    private String inNodeName;

    @TableField(exist = false)
    private String inBatchCode;

    /** 更新页"是否向上游发送确认请求"复选框（仅请求体使用，不入库） */
    @TableField(exist = false)
    private Boolean sendConfirm;
}

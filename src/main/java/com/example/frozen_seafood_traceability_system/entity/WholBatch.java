package com.example.frozen_seafood_traceability_system.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 批发商产品批号表 whol_batch（上游 = 加工企业）。
 * status: 1新建 2待确认 3已确认 4已下架
 */
@Data
@TableName("whol_batch")
public class WholBatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long nodeId;

    private String batchCode;

    private String productName;

    private String productType;

    private String inspectionCert;

    private String inspector;

    /** 进场：上游加工企业 id */
    private Long inNodeId;

    private String inArea;

    /** 进场：上游 proc_batch.id */
    private Long inBatchId;

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

    @TableField(exist = false)
    private Boolean sendConfirm;
}

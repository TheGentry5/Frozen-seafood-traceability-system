package com.example.frozen_seafood_traceability_system.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 零售商产品批号表 reta_batch（上游 = 批发商），比批发批号多 trace_code 溯源码。
 * status: 1新建 2待确认 3已确认 4已下架；已确认时自动生成溯源码。
 */
@Data
@TableName("reta_batch")
public class RetaBatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long nodeId;

    private String batchCode;

    private String productName;

    private String productType;

    private String inspectionCert;

    private String inspector;

    private Long inNodeId;

    private String inArea;

    private Long inBatchId;

    private String inProductName;

    /** 溯源标识码（已确认后生成，生成前为空） */
    private String traceCode;

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

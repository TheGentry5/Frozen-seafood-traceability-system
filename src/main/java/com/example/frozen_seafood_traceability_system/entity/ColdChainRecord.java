package com.example.frozen_seafood_traceability_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 冷链温度填报记录表 cold_chain_record，四环节共用。
 * batch_type: 1养殖 2加工 3批发 4零售
 */
@Data
@TableName("cold_chain_record")
public class ColdChainRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 环节类型：1养殖 2加工 3批发 4零售（对应 node_info.node_type） */
    private Integer batchType;

    /** 关联批号 id（按 batch_type 指向对应批次表） */
    private Long batchId;

    /** 填报企业 id（=该批号所属企业） */
    private Long nodeId;

    /** 摄氏温度 */
    private BigDecimal temperature;

    /** 湿度（%） */
    private BigDecimal humidity;

    /** 采集 / 填报时刻 */
    private LocalDateTime recordTime;

    private String remark;

    private LocalDateTime createTime;

    // ---- 联表 / 计算展示字段 ----
    @TableField(exist = false)
    private String stage;

    @TableField(exist = false)
    private String nodeName;
}

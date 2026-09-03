package com.example.frozen_seafood_traceability_system.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 养殖企业产品批号表 farm_batch。
 * status: 1待发布 2已发布 3已下架
 */
@Data
@TableName("farm_batch")
public class FarmBatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long nodeId;

    private String batchCode;

    /** 产品品种 */
    private String productName;

    private String inspectionCert;

    /** 官方检疫员名称 */
    private String inspector;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // ---- 联表展示字段 ----
    @TableField(exist = false)
    private String nodeName;

    /** 更新页"是否发布"复选框（仅请求体使用，不入库） */
    @TableField(exist = false)
    private Boolean publish;
}

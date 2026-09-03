package com.example.frozen_seafood_traceability_system.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 节点企业信息表 node_info。
 * node_type: 1养殖 2加工 3批发 4零售；status: 1启用 2停用
 */
@Data
@TableName("node_info")
public class NodeInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String nodeCode;

    private String password;

    private String nodeName;

    private Integer nodeType;

    private String provinceCode;

    private String cityCode;

    private String contact;

    private String phone;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // ---- 联表展示字段（exist=false，列表/下拉由自定义 SQL 填充） ----
    @TableField(exist = false)
    private String nodeTypeName;

    @TableField(exist = false)
    private String provinceName;

    @TableField(exist = false)
    private String cityName;
}

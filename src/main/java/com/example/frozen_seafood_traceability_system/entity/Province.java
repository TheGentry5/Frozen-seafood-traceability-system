package com.example.frozen_seafood_traceability_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 省行政区域表 province。
 */
@Data
@TableName("province")
public class Province {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String provinceCode;

    private String provinceName;
}

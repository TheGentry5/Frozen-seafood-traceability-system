package com.example.frozen_seafood_traceability_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 市行政区域表 city。
 */
@Data
@TableName("city")
public class City {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String cityCode;

    private String cityName;

    private String provinceCode;
}

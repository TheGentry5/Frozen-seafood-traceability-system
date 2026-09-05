package com.example.frozen_seafood_traceability_system.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.frozen_seafood_traceability_system.entity.City;
import com.example.frozen_seafood_traceability_system.entity.Province;
import com.example.frozen_seafood_traceability_system.mapper.CityMapper;
import com.example.frozen_seafood_traceability_system.mapper.ProvinceMapper;
import com.example.frozen_seafood_traceability_system.service.AreaService;

/**
 * 骨架占位：区域级联待业务实现（docs/开发实施文档.md §8 任务 5）。
 */
@Service
public class AreaServiceImpl implements AreaService {

    @Autowired
    private ProvinceMapper provinceMapper;

    @Autowired
    private CityMapper cityMapper;

    // 查省份
    @Override
    public List<Province> provinces() {
        return provinceMapper.selectList(new LambdaQueryWrapper<Province>().orderByAsc(Province::getId));
    }

    // 查市
    @Override
    public List<City> cities(String provinceCode) {
        if (provinceCode == null || provinceCode.isEmpty()) {
            return new ArrayList<>();
        }
        return cityMapper.selectList(new LambdaQueryWrapper<City>().eq(City::getProvinceCode, provinceCode)
                .orderByAsc(City::getId));
    }
}

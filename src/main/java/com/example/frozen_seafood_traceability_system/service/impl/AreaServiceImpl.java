package com.example.frozen_seafood_traceability_system.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.frozen_seafood_traceability_system.common.BizCode;
import com.example.frozen_seafood_traceability_system.common.BizException;
import com.example.frozen_seafood_traceability_system.entity.City;
import com.example.frozen_seafood_traceability_system.entity.Province;
import com.example.frozen_seafood_traceability_system.service.AreaService;

/**
 * 骨架占位：区域级联待业务实现（docs/开发实施文档.md §8 任务 5）。
 */
@Service
public class AreaServiceImpl implements AreaService {

    @Override
    public List<Province> provinces() {
        throw todo();
    }

    @Override
    public List<City> cities(String provinceCode) {
        throw todo();
    }

    private BizException todo() {
        return new BizException(BizCode.NOT_IMPLEMENTED, "接口待业务实现（编码阶段）");
    }
}

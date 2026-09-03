package com.example.frozen_seafood_traceability_system.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.frozen_seafood_traceability_system.common.Result;
import com.example.frozen_seafood_traceability_system.entity.City;
import com.example.frozen_seafood_traceability_system.entity.Province;
import com.example.frozen_seafood_traceability_system.service.AreaService;

/**
 * 省市级联（docs/开发实施文档.md §6.2）。
 */
@RestController
@RequestMapping("/api/area")
public class AreaController {

    @Autowired
    private AreaService areaService;

    @GetMapping("/provinces")
    public Result<List<Province>> provinces() {
        return Result.ok(areaService.provinces());
    }

    @GetMapping("/cities/{provinceCode}")
    public Result<List<City>> cities(@PathVariable String provinceCode) {
        return Result.ok(areaService.cities(provinceCode));
    }
}

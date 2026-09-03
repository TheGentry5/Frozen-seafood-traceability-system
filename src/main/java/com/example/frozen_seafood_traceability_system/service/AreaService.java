package com.example.frozen_seafood_traceability_system.service;

import java.util.List;

import com.example.frozen_seafood_traceability_system.entity.City;
import com.example.frozen_seafood_traceability_system.entity.Province;

/**
 * 省 / 市级联接口（docs/开发实施文档.md §6.2）。
 */
public interface AreaService {

    List<Province> provinces();

    List<City> cities(String provinceCode);
}

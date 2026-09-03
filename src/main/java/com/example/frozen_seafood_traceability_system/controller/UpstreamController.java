package com.example.frozen_seafood_traceability_system.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.frozen_seafood_traceability_system.common.Result;
import com.example.frozen_seafood_traceability_system.entity.NodeInfo;
import com.example.frozen_seafood_traceability_system.service.UpstreamService;

/**
 * 上游级联：省→市→企业→批号（docs/开发实施文档.md §6.2）。
 */
@RestController
@RequestMapping("/api/upstream")
public class UpstreamController {

    @Autowired
    private UpstreamService upstreamService;

    @GetMapping("/enterprises")
    public Result<List<NodeInfo>> enterprises(@RequestParam Integer nodeType,
                                              @RequestParam(required = false) String provinceCode,
                                              @RequestParam(required = false) String cityCode) {
        return Result.ok(upstreamService.enterprises(nodeType, provinceCode, cityCode));
    }

    @GetMapping("/batches")
    public Result<List<Map<String, Object>>> batches(@RequestParam Long nodeId,
                                                     @RequestParam Integer nodeType) {
        return Result.ok(upstreamService.batches(nodeId, nodeType));
    }
}

package com.example.frozen_seafood_traceability_system.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.frozen_seafood_traceability_system.common.Result;
import com.example.frozen_seafood_traceability_system.service.ColdChainService;
import com.example.frozen_seafood_traceability_system.service.TraceService;

/**
 * 消费者溯源（docs/开发实施文档.md §6.4）。GET /api/trace/info/** 为拦截器白名单。
 */
@RestController
@RequestMapping("/api/trace")
public class TraceController {

    @Autowired
    private TraceService traceService;

    @Autowired
    private ColdChainService coldChainService;

    @GetMapping("/info/{traceCode}")
    public Result<Map<String, Object>> info(@PathVariable String traceCode) {
        return Result.ok(traceService.trace(traceCode));
    }

    /** 全链冷链温度曲线（公开） */
    @GetMapping("/info/{traceCode}/temperature")
    public Result<Map<String, Object>> temperature(@PathVariable String traceCode) {
        return Result.ok(coldChainService.traceCurve(traceCode));
    }
}

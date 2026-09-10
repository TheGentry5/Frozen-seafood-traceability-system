package com.example.frozen_seafood_traceability_system.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.frozen_seafood_traceability_system.common.Result;
import com.example.frozen_seafood_traceability_system.entity.ColdChainRecord;
import com.example.frozen_seafood_traceability_system.service.ColdChainService;

/**
 * 冷链温度填报与曲线展示（扩展功能）。消费者全链曲线见 TraceController。
 */
@RestController
@RequestMapping("/api/cold-chain")
public class ColdChainController {

    @Autowired
    private ColdChainService coldChainService;

    @PostMapping("/report")
    public Result<Void> report(@RequestBody ColdChainRecord req) {
        coldChainService.report(req);
        return Result.ok();
    }

    @GetMapping
    public Result<List<ColdChainRecord>> list(@RequestParam Integer batchType,
                                              @RequestParam Long batchId) {
        return Result.ok(coldChainService.listMy(batchType, batchId));
    }

    @GetMapping("/curve")
    public Result<Map<String, Object>> curve(@RequestParam Integer batchType,
                                             @RequestParam Long batchId) {
        return Result.ok(coldChainService.curve(batchType, batchId));
    }
}

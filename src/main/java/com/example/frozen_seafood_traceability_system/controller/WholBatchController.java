package com.example.frozen_seafood_traceability_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.frozen_seafood_traceability_system.common.AuthContext;
import com.example.frozen_seafood_traceability_system.common.PageResult;
import com.example.frozen_seafood_traceability_system.common.Result;
import com.example.frozen_seafood_traceability_system.entity.RetaBatch;
import com.example.frozen_seafood_traceability_system.entity.WholBatch;
import com.example.frozen_seafood_traceability_system.service.RetaBatchService;
import com.example.frozen_seafood_traceability_system.service.WholBatchService;

/**
 * 批发商批号 + 下游（零售商）进场确认（docs/开发实施文档.md §6.3 whol 行）。
 */
@RestController
@RequestMapping("/api/whol")
public class WholBatchController {

    @Autowired
    private WholBatchService wholBatchService;

    @Autowired
    private RetaBatchService retaBatchService;

    @PostMapping("/batch")
    public Result<Void> create(@RequestBody WholBatch req) {
        wholBatchService.createMy(req);
        return Result.ok();
    }

    @GetMapping("/batch")
    public Result<PageResult<WholBatch>> page(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long size,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false) String keyword) {
        return Result.ok(wholBatchService.pageMy(page, size, status, keyword));
    }

    @GetMapping("/batch/exists")
    public Result<Boolean> exists(@RequestParam String batchCode,
                                  @RequestParam(required = false) Long id) {
        return Result.ok(wholBatchService.existsBatchCode(batchCode, id));
    }

    @GetMapping("/batch/{id}")
    public Result<WholBatch> detail(@PathVariable Long id) {
        return Result.ok(wholBatchService.detailMy(id));
    }

    @PutMapping("/batch/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody WholBatch req) {
        req.setId(id);
        wholBatchService.updateMy(req);
        return Result.ok();
    }

    @DeleteMapping("/batch/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        wholBatchService.deleteMy(id);
        return Result.ok();
    }

    @PutMapping("/batch/{id}/off")
    public Result<Void> off(@PathVariable Long id) {
        wholBatchService.offMy(id);
        return Result.ok();
    }

    // ===== 下游（零售商）进场确认：确认时触发溯源码生成 =====
    @GetMapping("/confirm")
    public Result<PageResult<RetaBatch>> confirmPage(@RequestParam(defaultValue = "1") long page,
                                                     @RequestParam(defaultValue = "10") long size,
                                                     @RequestParam(required = false) String keyword) {
        return Result.ok(retaBatchService.waitConfirmForWhol(AuthContext.nodeId(), keyword, page, size));
    }

    @PutMapping("/confirm/{id}")
    public Result<String> confirm(@PathVariable Long id) {
        return Result.ok(retaBatchService.confirmByWhol(id, AuthContext.nodeId()));
    }
}

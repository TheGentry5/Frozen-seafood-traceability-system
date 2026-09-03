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
import com.example.frozen_seafood_traceability_system.entity.FarmBatch;
import com.example.frozen_seafood_traceability_system.entity.ProcBatch;
import com.example.frozen_seafood_traceability_system.service.FarmBatchService;
import com.example.frozen_seafood_traceability_system.service.ProcBatchService;

/**
 * 养殖企业批号 + 下游（加工）进场确认（docs/开发实施文档.md §6.3 farm 行）。
 */
@RestController
@RequestMapping("/api/farm")
public class FarmBatchController {

    @Autowired
    private FarmBatchService farmBatchService;

    @Autowired
    private ProcBatchService procBatchService;

    @PostMapping("/batch")
    public Result<Void> create(@RequestBody FarmBatch req) {
        farmBatchService.createMy(req);
        return Result.ok();
    }

    @GetMapping("/batch")
    public Result<PageResult<FarmBatch>> page(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long size,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false) String keyword) {
        return Result.ok(farmBatchService.pageMy(page, size, status, keyword));
    }

    @GetMapping("/batch/exists")
    public Result<Boolean> exists(@RequestParam String batchCode,
                                  @RequestParam(required = false) Long id) {
        return Result.ok(farmBatchService.existsBatchCode(batchCode, id));
    }

    @GetMapping("/batch/{id}")
    public Result<FarmBatch> detail(@PathVariable Long id) {
        return Result.ok(farmBatchService.detailMy(id));
    }

    @PutMapping("/batch/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody FarmBatch req) {
        req.setId(id);
        farmBatchService.updateMy(req);
        return Result.ok();
    }

    @DeleteMapping("/batch/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        farmBatchService.deleteMy(id);
        return Result.ok();
    }

    @PutMapping("/batch/{id}/off")
    public Result<Void> off(@PathVariable Long id) {
        farmBatchService.offMy(id);
        return Result.ok();
    }

    // ===== 下游（加工企业）进场确认 =====
    @GetMapping("/confirm")
    public Result<PageResult<ProcBatch>> confirmPage(@RequestParam(defaultValue = "1") long page,
                                                     @RequestParam(defaultValue = "10") long size,
                                                     @RequestParam(required = false) String keyword) {
        return Result.ok(procBatchService.waitConfirmForFarm(AuthContext.nodeId(), keyword, page, size));
    }

    @PutMapping("/confirm/{id}")
    public Result<Void> confirm(@PathVariable Long id) {
        procBatchService.confirmByFarm(id, AuthContext.nodeId());
        return Result.ok();
    }
}

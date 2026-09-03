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
import com.example.frozen_seafood_traceability_system.entity.ProcBatch;
import com.example.frozen_seafood_traceability_system.entity.WholBatch;
import com.example.frozen_seafood_traceability_system.service.ProcBatchService;
import com.example.frozen_seafood_traceability_system.service.WholBatchService;

/**
 * 加工企业批号 + 下游（批发）进场确认（docs/开发实施文档.md §6.3 proc 行）。
 */
@RestController
@RequestMapping("/api/proc")
public class ProcBatchController {

    @Autowired
    private ProcBatchService procBatchService;

    @Autowired
    private WholBatchService wholBatchService;

    @PostMapping("/batch")
    public Result<Void> create(@RequestBody ProcBatch req) {
        procBatchService.createMy(req);
        return Result.ok();
    }

    @GetMapping("/batch")
    public Result<PageResult<ProcBatch>> page(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long size,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false) String keyword) {
        return Result.ok(procBatchService.pageMy(page, size, status, keyword));
    }

    @GetMapping("/batch/exists")
    public Result<Boolean> exists(@RequestParam String batchCode,
                                  @RequestParam(required = false) Long id) {
        return Result.ok(procBatchService.existsBatchCode(batchCode, id));
    }

    @GetMapping("/batch/{id}")
    public Result<ProcBatch> detail(@PathVariable Long id) {
        return Result.ok(procBatchService.detailMy(id));
    }

    @PutMapping("/batch/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ProcBatch req) {
        req.setId(id);
        procBatchService.updateMy(req);
        return Result.ok();
    }

    @DeleteMapping("/batch/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        procBatchService.deleteMy(id);
        return Result.ok();
    }

    @PutMapping("/batch/{id}/off")
    public Result<Void> off(@PathVariable Long id) {
        procBatchService.offMy(id);
        return Result.ok();
    }

    // ===== 下游（批发商）进场确认 =====
    @GetMapping("/confirm")
    public Result<PageResult<WholBatch>> confirmPage(@RequestParam(defaultValue = "1") long page,
                                                     @RequestParam(defaultValue = "10") long size,
                                                     @RequestParam(required = false) String keyword) {
        return Result.ok(wholBatchService.waitConfirmForProc(AuthContext.nodeId(), keyword, page, size));
    }

    @PutMapping("/confirm/{id}")
    public Result<Void> confirm(@PathVariable Long id) {
        wholBatchService.confirmByProc(id, AuthContext.nodeId());
        return Result.ok();
    }
}

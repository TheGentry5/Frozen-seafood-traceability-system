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

import com.example.frozen_seafood_traceability_system.common.PageResult;
import com.example.frozen_seafood_traceability_system.common.Result;
import com.example.frozen_seafood_traceability_system.entity.RetaBatch;
import com.example.frozen_seafood_traceability_system.service.RetaBatchService;

/**
 * 零售商批号（docs/开发实施文档.md §6.3 reta 行）。
 * 终端环节：无下游确认接口；详情含溯源码。
 */
@RestController
@RequestMapping("/api/reta")
public class RetaBatchController {

    @Autowired
    private RetaBatchService retaBatchService;

    @PostMapping("/batch")
    public Result<Void> create(@RequestBody RetaBatch req) {
        retaBatchService.createMy(req);
        return Result.ok();
    }

    @GetMapping("/batch")
    public Result<PageResult<RetaBatch>> page(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long size,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false) String keyword) {
        return Result.ok(retaBatchService.pageMy(page, size, status, keyword));
    }

    @GetMapping("/batch/exists")
    public Result<Boolean> exists(@RequestParam String batchCode,
                                  @RequestParam(required = false) Long id) {
        return Result.ok(retaBatchService.existsBatchCode(batchCode, id));
    }

    @GetMapping("/batch/{id}")
    public Result<RetaBatch> detail(@PathVariable Long id) {
        return Result.ok(retaBatchService.detailMy(id));
    }

    @PutMapping("/batch/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody RetaBatch req) {
        req.setId(id);
        retaBatchService.updateMy(req);
        return Result.ok();
    }

    @DeleteMapping("/batch/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        retaBatchService.deleteMy(id);
        return Result.ok();
    }

    @PutMapping("/batch/{id}/off")
    public Result<Void> off(@PathVariable Long id) {
        retaBatchService.offMy(id);
        return Result.ok();
    }
}

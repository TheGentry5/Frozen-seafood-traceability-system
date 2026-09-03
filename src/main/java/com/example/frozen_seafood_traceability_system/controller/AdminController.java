package com.example.frozen_seafood_traceability_system.controller;

import java.util.Map;

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
import com.example.frozen_seafood_traceability_system.entity.NodeInfo;
import com.example.frozen_seafood_traceability_system.service.AdminService;

/**
 * 系统管理端：节点企业注册信息管理（docs/开发实施文档.md §6.5）。
 * 管理员登录见 LoginController（POST /api/admin/login）。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/node")
    public Result<PageResult<NodeInfo>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size,
                                             @RequestParam(required = false) String nodeName,
                                             @RequestParam(required = false) Integer nodeType,
                                             @RequestParam(required = false) String provinceCode,
                                             @RequestParam(required = false) String cityCode) {
        return Result.ok(adminService.pageNode(page, size, nodeName, nodeType, provinceCode, cityCode));
    }

    @GetMapping("/node/{id}")
    public Result<NodeInfo> detail(@PathVariable Long id) {
        return Result.ok(adminService.detailNode(id));
    }

    @PostMapping("/node")
    public Result<Void> create(@RequestBody NodeInfo req) {
        adminService.createNode(req);
        return Result.ok();
    }

    @PutMapping("/node/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody NodeInfo req) {
        req.setId(id);
        adminService.updateNode(req);
        return Result.ok();
    }

    @DeleteMapping("/node/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminService.deleteNode(id);
        return Result.ok();
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.ok(adminService.stats());
    }
}

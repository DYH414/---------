package com.school.waimai.category.controller;

import com.school.waimai.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理端分类控制器
 */
@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    /**
     * 分类列表
     */
    @GetMapping
    public ApiResponse<?> list() {
        // TODO: 实现分类列表查询逻辑
        return ApiResponse.success();
    }

    /**
     * 新增分类
     */
    @PostMapping
    public ApiResponse<?> create(@RequestBody Object request) {
        // TODO: 实现新增分类逻辑
        return ApiResponse.success();
    }

    /**
     * 编辑分类
     */
    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody Object request) {
        // TODO: 实现编辑分类逻辑
        return ApiResponse.success();
    }

    /**
     * 禁用/启用分类
     */
    @PostMapping("/{id}/status")
    public ApiResponse<?> updateStatus(@PathVariable Long id, @RequestBody Object request) {
        // TODO: 实现分类状态更新逻辑
        return ApiResponse.success();
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        // TODO: 实现删除分类逻辑
        return ApiResponse.success();
    }
}

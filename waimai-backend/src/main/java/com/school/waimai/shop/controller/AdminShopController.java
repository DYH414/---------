package com.school.waimai.shop.controller;

import com.school.waimai.common.api.ApiResponse;
import com.school.waimai.common.api.PageResult;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理端商家控制器
 */
@RestController
@RequestMapping("/api/admin/shops")
public class AdminShopController {

    /**
     * 商家列表
     */
    @GetMapping
    public ApiResponse<PageResult<?>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort) {
        // TODO: 实现商家列表查询逻辑
        return ApiResponse.success();
    }

    /**
     * 新增商家
     */
    @PostMapping
    public ApiResponse<?> create(@RequestBody Object request) {
        // TODO: 实现新增商家逻辑
        return ApiResponse.success();
    }

    /**
     * 编辑商家
     */
    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody Object request) {
        // TODO: 实现编辑商家逻辑
        return ApiResponse.success();
    }

    /**
     * 删除商家（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        // TODO: 实现删除商家逻辑
        return ApiResponse.success();
    }

    /**
     * 商家上下架
     */
    @PostMapping("/{id}/status")
    public ApiResponse<?> updateStatus(@PathVariable Long id, @RequestBody Object request) {
        // TODO: 实现商家上下架逻辑
        return ApiResponse.success();
    }
}

package com.school.waimai.shop.controller;

import com.school.waimai.common.api.ApiResponse;
import com.school.waimai.common.api.PageResult;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序端商家控制器
 */
@RestController
@RequestMapping("/api/mp/shops")
public class MpShopController {

    /**
     * 商家列表
     */
    @GetMapping
    public ApiResponse<PageResult<?>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword) {
        // TODO: 实现商家列表查询逻辑
        return ApiResponse.success();
    }

    /**
     * 商家详情
     */
    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Long id) {
        // TODO: 实现商家详情查询逻辑
        return ApiResponse.success();
    }
}

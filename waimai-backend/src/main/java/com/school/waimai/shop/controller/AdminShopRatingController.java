package com.school.waimai.shop.controller;

import com.school.waimai.common.api.ApiResponse;
import com.school.waimai.common.api.PageResult;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理端商家评分统计控制器
 */
@RestController
@RequestMapping("/api/admin/shops")
public class AdminShopRatingController {

    /**
     * 评分统计查看
     */
    @GetMapping("/ratings")
    public ApiResponse<PageResult<?>> ratings(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        // TODO: 实现评分统计查询逻辑
        return ApiResponse.success();
    }
}

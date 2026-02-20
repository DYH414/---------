package com.school.waimai.recommend.controller;

import com.school.waimai.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理端每日推荐控制器
 */
@RestController
@RequestMapping("/api/admin/recommend")
public class AdminRecommendController {

    /**
     * 查询当日配置
     */
    @GetMapping("/daily-config")
    public ApiResponse<?> getDailyConfig() {
        // TODO: 实现查询每日推荐配置逻辑
        return ApiResponse.success();
    }

    /**
     * 保存配置
     */
    @PutMapping("/daily-config")
    public ApiResponse<?> saveDailyConfig(@RequestBody Object request) {
        // TODO: 实现保存每日推荐配置逻辑
        return ApiResponse.success();
    }
}

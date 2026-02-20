package com.school.waimai.recommend.controller;

import com.school.waimai.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序端每日推荐控制器
 */
@RestController
@RequestMapping("/api/mp/recommend")
public class MpRecommendController {

    /**
     * 每日推荐
     */
    @GetMapping("/daily")
    public ApiResponse<?> daily() {
        // TODO: 实现每日推荐查询逻辑
        return ApiResponse.success();
    }
}

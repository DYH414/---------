package com.school.waimai.rating.controller;

import com.school.waimai.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序端评分控制器
 */
@RestController
@RequestMapping("/api/mp/shops/{shopId}/rating")
public class MpRatingController {

    /**
     * 提交/更新评分
     */
    @PostMapping
    public ApiResponse<?> submitRating(@PathVariable Long shopId, @RequestBody Object request) {
        // TODO: 实现评分提交逻辑
        return ApiResponse.success();
    }
}

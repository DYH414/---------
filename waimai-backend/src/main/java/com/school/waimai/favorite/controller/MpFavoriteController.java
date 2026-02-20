package com.school.waimai.favorite.controller;

import com.school.waimai.common.api.ApiResponse;
import com.school.waimai.common.api.PageResult;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序端收藏控制器
 */
@RestController
@RequestMapping("/api/mp")
public class MpFavoriteController {

    /**
     * 收藏商家
     */
    @PostMapping("/shops/{shopId}/favorite")
    public ApiResponse<?> favorite(@PathVariable Long shopId) {
        // TODO: 实现收藏商家逻辑
        return ApiResponse.success();
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/shops/{shopId}/favorite")
    public ApiResponse<?> unfavorite(@PathVariable Long shopId) {
        // TODO: 实现取消收藏逻辑
        return ApiResponse.success();
    }

    /**
     * 我的收藏列表
     */
    @GetMapping("/user/favorites")
    public ApiResponse<PageResult<?>> favorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 实现收藏列表查询逻辑
        return ApiResponse.success();
    }
}

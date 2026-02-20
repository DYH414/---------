package com.school.waimai.auth.controller;

import com.school.waimai.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序端鉴权控制器
 */
@RestController
@RequestMapping("/api/mp/auth")
public class MpAuthController {

    /**
     * 小程序登录
     */
    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody Object request) {
        // TODO: 实现微信登录逻辑
        return ApiResponse.success();
    }
}

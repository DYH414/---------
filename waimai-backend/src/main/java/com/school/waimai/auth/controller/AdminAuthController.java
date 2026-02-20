package com.school.waimai.auth.controller;

import com.school.waimai.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理端鉴权控制器
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody Object request) {
        // TODO: 实现管理员登录逻辑
        return ApiResponse.success();
    }
}

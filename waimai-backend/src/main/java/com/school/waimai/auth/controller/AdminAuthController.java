package com.school.waimai.auth.controller;

import com.school.waimai.auth.dto.AdminLoginRequest;
import com.school.waimai.auth.dto.AdminLoginResponse;
import com.school.waimai.auth.service.AdminAuthService;
import com.school.waimai.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台管理端鉴权控制器
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {


    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = adminAuthService.login(request);
        return ApiResponse.success(response);
    }
}

package com.school.waimai.auth.service;

import com.school.waimai.auth.dto.AdminLoginRequest;
import com.school.waimai.auth.dto.AdminLoginResponse;

public interface AdminAuthService {

    /**
     * 管理员登录
     */
    AdminLoginResponse login(AdminLoginRequest request);
}

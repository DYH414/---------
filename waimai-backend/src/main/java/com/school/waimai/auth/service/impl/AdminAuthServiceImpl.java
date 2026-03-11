package com.school.waimai.auth.service.impl;

import com.school.waimai.auth.dto.AdminLoginRequest;
import com.school.waimai.auth.dto.AdminLoginResponse;
import com.school.waimai.auth.entity.AdminUser;
import com.school.waimai.auth.mapper.AdminUserMapper;
import com.school.waimai.auth.service.AdminAuthService;
import com.school.waimai.common.api.ErrorCode;
import com.school.waimai.common.exception.BizException;
import com.school.waimai.common.util.JwtUtil;
import com.school.waimai.common.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminUserMapper adminUserMapper;
    private final JwtUtil jwtUtil;

    public AdminAuthServiceImpl(AdminUserMapper adminUserMapper, JwtUtil jwtUtil) {
        this.adminUserMapper = adminUserMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        AdminUser admin = adminUserMapper.selectByUsernameNotDeleted(request.getUsername());

        if (admin == null) {
            throw new BizException(ErrorCode.Auth.ERR_ADMIN_LOGIN_FAILED);
        }
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new BizException(ErrorCode.Auth.ERR_ADMIN_DISABLED);
        }

        boolean passwordValid = PasswordUtil.verifyPassword(
                request.getPassword(),
                admin.getSalt(),
                admin.getPasswordHash());
        if (!passwordValid) {
            throw new BizException(ErrorCode.Auth.ERR_ADMIN_LOGIN_FAILED);
        }

        LocalDateTime now = LocalDateTime.now();
        admin.setLastLoginTime(now);
        adminUserMapper.updateLastLoginTime(admin.getId(), now);

        String token = jwtUtil.generateAdminToken(admin.getId(), admin.getUsername());
        return new AdminLoginResponse(token, admin.getUsername());
    }
}

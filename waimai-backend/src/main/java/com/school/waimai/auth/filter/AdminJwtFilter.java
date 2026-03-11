package com.school.waimai.auth.filter;

import com.school.waimai.common.api.ErrorCode;
import com.school.waimai.common.exception.BizException;
import com.school.waimai.common.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 后台管理端简单 JWT 校验过滤器
 *
 * 规则：
 * - 只拦截 /api/admin/** 请求
 * - 放行登录接口 /api/admin/auth/login
 * - 其它管理端接口必须携带合法的 JWT token
 */
@Component
public class AdminJwtFilter extends OncePerRequestFilter {

    private static final String ADMIN_PREFIX = "/api/admin/";
    private static final String ADMIN_LOGIN_PATH = "/api/admin/auth/login";

    private final JwtUtil jwtUtil;

    public AdminJwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // 非后台管理端接口，直接放行
        if (!path.startsWith(ADMIN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 登录接口直接放行
        if (ADMIN_LOGIN_PATH.equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从 Authorization 头中获取 token，格式：Bearer xxx
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            throw new BizException(ErrorCode.Auth.ERR_UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        try {
            // 仅校验合法性和是否过期，解析异常会抛出
            jwtUtil.parseToken(token);
        } catch (ExpiredJwtException e) {
            throw new BizException(ErrorCode.Auth.ERR_TOKEN_EXPIRED);
        } catch (JwtException e) {
            throw new BizException(ErrorCode.Auth.ERR_TOKEN_INVALID);
        }

        // 校验通过，继续后续处理
        filterChain.doFilter(request, response);
    }
}

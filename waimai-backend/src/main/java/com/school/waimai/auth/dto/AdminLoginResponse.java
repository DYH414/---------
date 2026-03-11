package com.school.waimai.auth.dto;

/**
 * 管理员登录响应数据
 */
public class AdminLoginResponse {

    /**
     * JWT 访问令牌
     */
    private String token;

    /**
     * 管理员用户名
     */
    private String username;

    public AdminLoginResponse() {
    }

    public AdminLoginResponse(String token, String username) {
        this.token = token;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

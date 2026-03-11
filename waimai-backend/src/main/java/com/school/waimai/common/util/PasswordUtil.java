package com.school.waimai.common.util;

import cn.hutool.core.util.RandomUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 密码工具类：生成 salt、hash 与验证
 * 约定：password_hash = Base64(SHA-256(password + salt))
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * 生成随机盐值
     */
    public static String generateSalt() {
        return RandomUtil.randomString(32);
    }

    /**
     * 计算密码哈希
     *
     * @param password 明文密码
     * @param salt     盐值
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((password + salt).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 校验密码是否匹配
     */
    public static boolean verifyPassword(String rawPassword, String salt, String storedHash) {
        if (rawPassword == null || salt == null || storedHash == null) {
            return false;
        }
        String calculated = hashPassword(rawPassword, salt);
        return constantTimeEquals(calculated, storedHash);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}

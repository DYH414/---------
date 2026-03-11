package com.school.waimai.auth.util;

import com.school.waimai.common.util.PasswordUtil;

/**
 * 管理员密码生成工具（仅用于本地/测试环境生成 hash 和 salt）。
 * 运行 main 方法后，将控制台输出的 SQL 在 MySQL 中执行，即可用 admin / admin123 登录。
 */
public final class AdminPasswordGenerator {

    public static void main(String[] args) {
        String password = "admin123";
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        System.out.println("=== 管理员密码生成结果 ===");
        System.out.println("用户名: admin");
        System.out.println("密码:   " + password);
        System.out.println("Salt:   " + salt);
        System.out.println("Hash:   " + hash);
        System.out.println();
        System.out.println("--- 在 MySQL 中执行以下 SQL（二选一）---");
        System.out.println();
        System.out.println("-- 若 admin 用户已存在（如已执行过 init_data.sql），执行：");
        System.out.printf("UPDATE `admin_user` SET `password_hash` = '%s', `salt` = '%s' WHERE `username` = 'admin';%n",
                hash, salt);
        System.out.println();
        System.out.println("-- 若 admin 用户不存在，执行：");
        System.out.printf(
                "INSERT INTO `admin_user` (`username`, `password_hash`, `salt`, `status`) VALUES ('admin', '%s', '%s', 1);%n",
                hash, salt);
    }
}

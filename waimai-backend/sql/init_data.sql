-- ============================================
-- 校园外卖信息聚合平台 - 初始数据脚本
-- 说明：插入系统初始化数据（默认分类、默认管理员等）
-- ============================================

USE `waimai`;

-- ============================================
-- 1. 插入默认分类数据
-- ============================================

INSERT INTO `shop_category` (`name`, `logo_url`, `sort_order`, `status`, `create_time`, `update_time`) VALUES
('快餐', NULL, 10, 1, NOW(), NOW()),
('奶茶', NULL, 9, 1, NOW(), NOW()),
('正餐', NULL, 8, 1, NOW(), NOW()),
('夜宵', NULL, 7, 1, NOW(), NOW()),
('其他', NULL, 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- ============================================
-- 2. 插入默认管理员账号
-- ============================================
-- 注意：密码为 "admin123"，需要先使用密码加密工具生成hash和salt
-- 这里提供一个示例，实际使用时需要替换为真实的加密后的密码
-- 密码加密示例（使用SHA-256 + salt）：
-- salt: 随机生成
-- password_hash: SHA256(password + salt) 的Base64编码

-- 默认管理员账号：admin / admin123
-- 以下密码hash和salt为示例值，实际部署时需要替换
-- 可以使用 PasswordUtil.generateSalt() 和 PasswordUtil.hashPassword() 生成

INSERT INTO `admin_user` (`username`, `password_hash`, `salt`, `status`, `create_time`, `update_time`) VALUES
('admin', 'YOUR_PASSWORD_HASH_HERE', 'YOUR_SALT_HERE', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- ============================================
-- 3. 插入示例商家数据（可选，用于测试）
-- ============================================

-- 示例商家1
INSERT INTO `shop` (`name`, `category_id`, `introduction`, `starting_price`, `delivery_fee_desc`, `status`, `campus_code`, `create_time`, `update_time`) VALUES
('校内麻辣烫', 1, '正宗川味麻辣烫，食材新鲜，口味地道', 15.00, '配送费约3元', 1, 'DEFAULT', NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 获取刚插入的商家ID（用于插入平台链接）
SET @shop_id_1 = LAST_INSERT_ID();

-- 示例商家1的平台链接
INSERT INTO `shop_platform_link` (`shop_id`, `platform_type`, `link_type`, `h5_url`, `status`, `create_time`, `update_time`) VALUES
(@shop_id_1, 1, 1, 'https://waimai.meituan.com/example', 1, NOW(), NOW()),
(@shop_id_1, 2, 1, 'https://www.ele.me/example', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 示例商家2
INSERT INTO `shop` (`name`, `category_id`, `introduction`, `starting_price`, `delivery_fee_desc`, `status`, `campus_code`, `create_time`, `update_time`) VALUES
('校园奶茶店', 2, '新鲜水果，现做现卖，口感丰富', 10.00, '免配送费', 1, 'DEFAULT', NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

SET @shop_id_2 = LAST_INSERT_ID();

INSERT INTO `shop_platform_link` (`shop_id`, `platform_type`, `link_type`, `mini_program_path`, `status`, `create_time`, `update_time`) VALUES
(@shop_id_2, 4, 3, '/pages/shop/index?id=' || @shop_id_2, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- ============================================
-- 4. 插入默认每日推荐配置（可选）
-- ============================================

-- 插入今日的随机推荐配置（默认4个）
INSERT INTO `daily_recommend_config` (`biz_date`, `mode`, `random_count`, `create_time`, `update_time`) VALUES
(CURDATE(), 2, 4, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

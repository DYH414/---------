-- ============================================
-- 校园外卖信息聚合平台 - 数据库建表脚本
-- 数据库版本：MySQL 8.0+
-- 字符集：utf8mb4
-- 存储引擎：InnoDB
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `waimai` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `waimai`;

-- ============================================
-- 1. 用户与管理员表
-- ============================================

-- 1.1 小程序用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `openid` VARCHAR(64) NOT NULL COMMENT '微信openid，用户唯一标识',
    `unionid` VARCHAR(64) DEFAULT NULL COMMENT '微信unionid，可空',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1=正常，0=禁用',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最近登录时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_openid` (`openid`),
    KEY `idx_user_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='小程序用户表';

-- 1.2 管理员表
CREATE TABLE IF NOT EXISTS `admin_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(32) NOT NULL COMMENT '登录名（唯一）',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `salt` VARCHAR(64) DEFAULT NULL COMMENT '加盐，可选',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1=正常，0=禁用',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最近登录时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='管理员表';

-- ============================================
-- 2. 商家与分类表
-- ============================================

-- 2.1 商家分类表
CREATE TABLE IF NOT EXISTS `shop_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(32) NOT NULL COMMENT '分类名称',
    `logo_url` VARCHAR(255) DEFAULT NULL COMMENT '分类Logo/图标URL',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值，越大越靠前',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_name` (`name`),
    KEY `idx_category_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家分类表';

-- 2.2 商家表
CREATE TABLE IF NOT EXISTS `shop` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(60) NOT NULL COMMENT '商家名称（2～30字）',
    `category_id` BIGINT NOT NULL COMMENT '分类ID，关联shop_category.id',
    `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面图/Logo URL',
    `introduction` VARCHAR(200) DEFAULT NULL COMMENT '商家介绍（用于首页小字展示）',
    `business_hours` VARCHAR(128) DEFAULT NULL COMMENT '营业时间说明',
    `delivery_scope` VARCHAR(128) DEFAULT NULL COMMENT '配送范围说明',
    `starting_price` DECIMAL(10,2) DEFAULT NULL COMMENT '起送价',
    `delivery_fee_desc` VARCHAR(64) DEFAULT NULL COMMENT '配送费说明',
    `sort_weight` INT NOT NULL DEFAULT 0 COMMENT '排序权重，默认0',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：1=上架，0=下架',
    `campus_code` VARCHAR(32) NOT NULL DEFAULT 'DEFAULT' COMMENT '校区标识，单校区可固定一个值',
    `avg_score` DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '平均评分（1.00～5.00），默认0.00',
    `rating_count` INT NOT NULL DEFAULT 0 COMMENT '评分人数，默认0',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '后台备注，前台不展示',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=删除',
    PRIMARY KEY (`id`),
    KEY `idx_shop_category_status` (`category_id`, `status`),
    KEY `idx_shop_status_update` (`status`, `update_time`),
    KEY `idx_shop_sort_weight` (`sort_weight` DESC),
    KEY `idx_shop_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家表';

-- 2.3 商家平台链接表
CREATE TABLE IF NOT EXISTS `shop_platform_link` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `shop_id` BIGINT NOT NULL COMMENT '商家ID，关联shop.id',
    `platform_type` TINYINT NOT NULL COMMENT '所属平台：1=美团，2=饿了么，3=京东，4=小程序自营，9=其他',
    `link_type` TINYINT NOT NULL COMMENT '链接类型：1=H5，2=外部小程序，3=内部小程序path',
    `h5_url` VARCHAR(512) DEFAULT NULL COMMENT 'H5跳转链接，link_type=1时使用',
    `mini_program_appid` VARCHAR(64) DEFAULT NULL COMMENT '外部小程序appid，link_type=2时使用',
    `mini_program_path` VARCHAR(255) DEFAULT NULL COMMENT '小程序path，自营或外部均可使用',
    `is_primary` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主推入口：1=是，0=否，默认0',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1=有效，0=失效',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=删除',
    PRIMARY KEY (`id`),
    KEY `idx_platform_shop` (`platform_type`, `shop_id`),
    KEY `idx_link_shop` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家平台链接表';

-- ============================================
-- 3. 评分与收藏表
-- ============================================

-- 3.1 商家评分表
CREATE TABLE IF NOT EXISTS `shop_rating` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID，关联user.id',
    `shop_id` BIGINT NOT NULL COMMENT '商家ID，关联shop.id',
    `score` TINYINT NOT NULL COMMENT '星级评分：1～5',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（首次评分时间）',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（最近评分时间）',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rating_user_shop` (`user_id`, `shop_id`),
    KEY `idx_rating_shop` (`shop_id`),
    KEY `idx_rating_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家评分表';

-- 3.2 商家收藏表
CREATE TABLE IF NOT EXISTS `shop_favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID，关联user.id',
    `shop_id` BIGINT NOT NULL COMMENT '商家ID，关联shop.id',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_favorite_user_shop` (`user_id`, `shop_id`),
    KEY `idx_favorite_user` (`user_id`, `create_time` DESC),
    KEY `idx_favorite_shop` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家收藏表';

-- ============================================
-- 4. 每日推荐配置表
-- ============================================

CREATE TABLE IF NOT EXISTS `daily_recommend_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `biz_date` DATE NOT NULL COMMENT '业务日期（自然日，如2025-02-09），同一天仅一条记录',
    `mode` TINYINT NOT NULL COMMENT '模式：1=手动，2=随机',
    `manual_shop_ids` TEXT DEFAULT NULL COMMENT '手动模式时的商家ID列表（JSON数组），mode=1时使用',
    `random_count` INT DEFAULT 4 COMMENT '随机模式展示数量N，默认4',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_daily_biz_date` (`biz_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='每日推荐配置表';

-- ============================================
-- 5. 操作日志表
-- ============================================

CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `admin_id` BIGINT NOT NULL COMMENT '管理员ID，关联admin_user.id',
    `action` VARCHAR(64) NOT NULL COMMENT '操作类型编码，如SHOP_CREATE、SHOP_UPDATE等',
    `target_type` VARCHAR(32) NOT NULL COMMENT '目标类型，如SHOP、CATEGORY',
    `target_id` BIGINT DEFAULT NULL COMMENT '目标对象ID',
    `request_data` TEXT DEFAULT NULL COMMENT '操作请求数据（JSON），建议只存必要业务字段',
    `diff_data` TEXT DEFAULT NULL COMMENT '变更前后差异（可选）',
    `ip` VARCHAR(64) DEFAULT NULL COMMENT '操作IP',
    `user_agent` VARCHAR(255) DEFAULT NULL COMMENT 'UA信息，可选',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_log_admin_time` (`admin_id`, `create_time` DESC),
    KEY `idx_log_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

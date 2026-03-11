-- ============================================
-- 测试数据脚本（可选）
-- 说明：用于开发和测试环境，插入一些测试数据
-- ============================================

USE `waimai`;

-- ============================================
-- 插入测试商家数据
-- ============================================

-- 测试商家1：快餐类
INSERT INTO `shop` (`name`, `category_id`, `introduction`, `starting_price`, `delivery_fee_desc`, `sort_weight`, `status`, `campus_code`) VALUES
('测试麻辣烫店', 1, '测试商家介绍：正宗川味麻辣烫', 15.00, '配送费3元', 10, 1, 'DEFAULT'),
('测试快餐店', 1, '测试商家介绍：快速便捷的快餐', 12.00, '配送费2元', 9, 1, 'DEFAULT'),
('测试奶茶店', 2, '测试商家介绍：新鲜水果奶茶', 10.00, '免配送费', 8, 1, 'DEFAULT'),
('测试正餐店', 3, '测试商家介绍：精致正餐', 30.00, '配送费5元', 7, 1, 'DEFAULT'),
('测试夜宵店', 4, '测试商家介绍：深夜美食', 20.00, '配送费4元', 6, 1, 'DEFAULT')
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 获取插入的商家ID
SET @test_shop_1 = (SELECT id FROM shop WHERE name = '测试麻辣烫店' LIMIT 1);
SET @test_shop_2 = (SELECT id FROM shop WHERE name = '测试快餐店' LIMIT 1);
SET @test_shop_3 = (SELECT id FROM shop WHERE name = '测试奶茶店' LIMIT 1);

-- 插入测试商家的平台链接
INSERT INTO `shop_platform_link` (`shop_id`, `platform_type`, `link_type`, `h5_url`, `status`) VALUES
(@test_shop_1, 1, 1, 'https://waimai.meituan.com/test-shop-1', 1),
(@test_shop_1, 2, 1, 'https://www.ele.me/test-shop-1', 1),
(@test_shop_2, 1, 1, 'https://waimai.meituan.com/test-shop-2', 1),
(@test_shop_3, 4, 3, '/pages/shop/index?id=' || @test_shop_3, 1)
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- ============================================
-- 插入测试用户数据（可选）
-- ============================================

-- 注意：实际测试用户应该通过小程序登录接口创建
-- 这里仅作为示例

-- ============================================
-- 插入测试评分数据（可选）
-- ============================================

-- 注意：需要先有测试用户数据
-- INSERT INTO `shop_rating` (`user_id`, `shop_id`, `score`) VALUES
-- (1, @test_shop_1, 5),
-- (1, @test_shop_2, 4);

-- ============================================
-- 插入测试收藏数据（可选）
-- ============================================

-- 注意：需要先有测试用户数据
-- INSERT INTO `shop_favorite` (`user_id`, `shop_id`) VALUES
-- (1, @test_shop_1),
-- (1, @test_shop_2);

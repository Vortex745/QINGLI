-- 更新商品表，添加缺失的字段
-- 请在数据库中执行此脚本
-- 如果某个字段已存在，会报错但不影响其他字段，可以忽略该错误
-- 添加 view_count 字段 (浏览量)
ALTER TABLE bus_goods
ADD COLUMN view_count INT DEFAULT 0 COMMENT '浏览量';
-- 添加 original_price 字段 (原价)
ALTER TABLE bus_goods
ADD COLUMN original_price DECIMAL(10, 2) DEFAULT NULL COMMENT '原价';
-- 添加 category 字段 (分类)
ALTER TABLE bus_goods
ADD COLUMN category VARCHAR(50) DEFAULT NULL COMMENT '分类';
-- 添加 goods_condition 字段 (成色)
ALTER TABLE bus_goods
ADD COLUMN goods_condition VARCHAR(50) DEFAULT NULL COMMENT '成色';
-- 添加 trading_method 字段 (交易方式)
ALTER TABLE bus_goods
ADD COLUMN trading_method VARCHAR(50) DEFAULT NULL COMMENT '交易方式';
-- 添加 location 字段 (交易地点)
ALTER TABLE bus_goods
ADD COLUMN location VARCHAR(100) DEFAULT NULL COMMENT '交易地点';
-- 添加 bargain_allowed 字段 (是否接受议价)
ALTER TABLE bus_goods
ADD COLUMN bargain_allowed INT DEFAULT 0 COMMENT '是否接受议价 1-是 0-否';
-- 添加 want_count 字段 ("我想要"点击次数)
ALTER TABLE bus_goods
ADD COLUMN want_count INT DEFAULT 0 COMMENT '"我想要"点击次数';
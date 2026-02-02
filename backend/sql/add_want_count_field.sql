-- 添加商品"想要"计数字段
ALTER TABLE bus_goods
ADD COLUMN want_count INT DEFAULT 0 COMMENT '"我想要"点击次数';
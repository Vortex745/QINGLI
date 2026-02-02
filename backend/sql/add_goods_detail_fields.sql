-- 为 bus_goods 表添加扩展字段以支持详细商品信息
ALTER TABLE `bus_goods`
ADD COLUMN `original_price` DECIMAL(10, 2) NULL COMMENT '原价'
AFTER `view_count`,
    ADD COLUMN `category` VARCHAR(50) NULL COMMENT '分类'
AFTER `original_price`,
    ADD COLUMN `condition` VARCHAR(50) NULL COMMENT '成色'
AFTER `category`,
    ADD COLUMN `trading_method` VARCHAR(50) NULL COMMENT '交易方式'
AFTER `condition`,
    ADD COLUMN `location` VARCHAR(200) NULL COMMENT '交易地点'
AFTER `trading_method`,
    ADD COLUMN `bargain_allowed` TINYINT(1) DEFAULT 1 COMMENT '是否接受议价 1-是 0-否'
AFTER `location`;
-- 重命名 condition 列以避免 MySQL 关键字冲突
ALTER TABLE `bus_goods` CHANGE COLUMN `condition` `goods_condition` VARCHAR(50) NULL COMMENT '成色';
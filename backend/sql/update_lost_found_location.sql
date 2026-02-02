ALTER TABLE `bus_lost_found`
ADD COLUMN `university` VARCHAR(50) DEFAULT '浙江大学' COMMENT '所属校区';
UPDATE `bus_lost_found`
SET `university` = '浙江大学'
WHERE `university` IS NULL;
-- 为 bus_post 表添加 location 字段
ALTER TABLE `bus_post`
ADD COLUMN `location` varchar(100) DEFAULT NULL COMMENT '校区/地点'
AFTER `is_anonymous`;
-- 为 part_time_job 表添加 location 字段 (用于校区筛选，与 work_location 区分)
ALTER TABLE `part_time_job`
ADD COLUMN `location` varchar(100) DEFAULT NULL COMMENT '校区/地点'
AFTER `status`;
-- 更新所有现有数据的 location 字段为 '浙江大学'
UPDATE `bus_goods`
SET `location` = '浙江大学'
WHERE `location` IS NULL
    OR `location` = '';
UPDATE `bus_post`
SET `location` = '浙江大学'
WHERE `location` IS NULL
    OR `location` = '';
UPDATE `part_time_job`
SET `location` = '浙江大学'
WHERE `location` IS NULL
    OR `location` = '';
-- 验证更新结果
SELECT 'bus_goods' AS table_name,
    COUNT(*) AS total,
    SUM(
        CASE
            WHEN location = '浙江大学' THEN 1
            ELSE 0
        END
    ) AS updated
FROM bus_goods
UNION ALL
SELECT 'bus_post' AS table_name,
    COUNT(*) AS total,
    SUM(
        CASE
            WHEN location = '浙江大学' THEN 1
            ELSE 0
        END
    ) AS updated
FROM bus_post
UNION ALL
SELECT 'part_time_job' AS table_name,
    COUNT(*) AS total,
    SUM(
        CASE
            WHEN location = '浙江大学' THEN 1
            ELSE 0
        END
    ) AS updated
FROM part_time_job;
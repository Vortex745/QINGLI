-- Add sys_report table
DROP TABLE IF EXISTS `sys_report`;
CREATE TABLE `sys_report` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    `reporter_id` bigint NOT NULL COMMENT 'Reporter User ID',
    `target_id` bigint NOT NULL COMMENT 'Target ID (Post/Goods/Comment/User)',
    `type` int NOT NULL COMMENT 'Type: 1-Goods, 2-Post, 3-User, 4-Comment',
    `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT 'Report Reason',
    `status` int DEFAULT 0 COMMENT 'Status: 0-Pending, 1-Handled, 2-Ignored',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_reporter`(`reporter_id` ASC) USING BTREE,
    INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Report Management Table' ROW_FORMAT = Dynamic;
-- Add sys_notice table
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Notice Title',
    `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Notice Content',
    `type` int DEFAULT 0 COMMENT 'Type: 0-All Users, 1-Specific User',
    `target_id` bigint DEFAULT NULL COMMENT 'Target User ID (if specific)',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'System Notice Table' ROW_FORMAT = Dynamic;
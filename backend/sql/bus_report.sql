-- 创建举报表
CREATE TABLE IF NOT EXISTS `bus_report` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `reporter_id` bigint NOT NULL COMMENT '举报人ID',
    `target_type` int NOT NULL COMMENT '被举报对象类型: 1-商品, 2-动态, 3-兼职, 4-失物招领, 5-用户',
    `target_id` bigint NOT NULL COMMENT '被举报对象ID',
    `target_user_id` bigint DEFAULT NULL COMMENT '被举报用户ID',
    `report_type` int NOT NULL COMMENT '举报类型: 1-虚假信息, 2-诈骗, 3-违禁品, 4-色情低俗, 5-骚扰辱骂, 6-其他',
    `reason` varchar(500) NOT NULL COMMENT '举报理由详情',
    `status` int NOT NULL DEFAULT '0' COMMENT '处理状态: 0-待处理, 1-已处理, 2-已驳回',
    `handle_result` varchar(500) DEFAULT NULL COMMENT '处理结果说明',
    `handler_id` bigint DEFAULT NULL COMMENT '处理人ID',
    `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
    `is_delete` int NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_reporter_id` (`reporter_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '举报信息表';
-- 创建聊天消息表
-- 请在数据库中执行此脚本
CREATE TABLE IF NOT EXISTS `bus_chat_message` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sender_id` bigint NOT NULL COMMENT '发送者ID',
    `receiver_id` bigint NOT NULL COMMENT '接收者ID',
    `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
    `type` int DEFAULT '0' COMMENT '消息类型: 0-文本, 1-图片, 2-商品卡片, 9-系统通知',
    `goods_id` bigint DEFAULT NULL COMMENT '关联商品ID(用于商品卡片消息)',
    `is_read` int DEFAULT '0' COMMENT '是否已读: 0-未读, 1-已读',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
    KEY `idx_receiver_read` (`receiver_id`, `is_read`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天消息表';
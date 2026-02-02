/*
 Navicat Premium Dump SQL

 Source Server         : mydb
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : xianqu_pdb

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 31/01/2026 20:26:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for bus_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `bus_chat_message`;
CREATE TABLE `bus_chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `type` int NULL DEFAULT 0 COMMENT '消息类型: 0-文本, 1-图片, 2-商品卡片, 9-系统通知',
  `goods_id` bigint NULL DEFAULT NULL COMMENT '关联商品ID(用于商品卡片消息)',
  `is_read` int NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sender_receiver`(`sender_id` ASC, `receiver_id` ASC) USING BTREE,
  INDEX `idx_receiver_read`(`receiver_id` ASC, `is_read` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bus_chat_message
-- ----------------------------

-- ----------------------------
-- Table structure for bus_comment
-- ----------------------------
DROP TABLE IF EXISTS `bus_comment`;
CREATE TABLE `bus_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '评论者ID',
  `target_id` bigint NOT NULL COMMENT '目标ID(商品/帖子ID)',
  `type` int NOT NULL COMMENT '1-商品, 2-帖子',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `is_delete` int NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_target_type`(`target_id` ASC, `type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bus_comment
-- ----------------------------
INSERT INTO `bus_comment` VALUES (1, 1, 2, 1, '你好呀', 0, '2026-01-25 20:40:08');
INSERT INTO `bus_comment` VALUES (2, 1, 2, 1, '123321', 0, '2026-01-25 20:41:46');
INSERT INTO `bus_comment` VALUES (3, 1, 1, 7, '这个不好', 0, '2026-01-25 20:48:17');
INSERT INTO `bus_comment` VALUES (4, 1, 1, 7, '别来', 0, '2026-01-25 20:48:20');
INSERT INTO `bus_comment` VALUES (5, 1, 1, 5, '傻逼', 0, '2026-01-25 21:03:37');
INSERT INTO `bus_comment` VALUES (6, 1, 1, 7, '123', 0, '2026-01-25 22:57:40');
INSERT INTO `bus_comment` VALUES (7, 1, 1, 7, '1111111111111111111111111111111111111', 0, '2026-01-25 22:57:51');
INSERT INTO `bus_comment` VALUES (8, 1, 1, 5, '111', 0, '2026-01-25 22:59:16');
INSERT INTO `bus_comment` VALUES (9, 1, 1, 7, '1111', 0, '2026-01-25 23:04:26');
INSERT INTO `bus_comment` VALUES (10, 1, 1, 7, '1111', 0, '2026-01-25 23:04:28');
INSERT INTO `bus_comment` VALUES (11, 1, 1, 7, '2222', 0, '2026-01-25 23:04:32');
INSERT INTO `bus_comment` VALUES (12, 1, 2, 1, '123', 0, '2026-01-29 17:33:07');
INSERT INTO `bus_comment` VALUES (13, 1, 1, 5, '1231', 0, '2026-01-29 17:33:19');
INSERT INTO `bus_comment` VALUES (14, 1, 1, 7, '123', 0, '2026-01-30 21:04:40');
INSERT INTO `bus_comment` VALUES (15, 1, 4, 7, '123123', 0, '2026-01-31 16:47:19');
INSERT INTO `bus_comment` VALUES (16, 1, 2, 5, '12312', 0, '2026-01-31 16:48:59');
INSERT INTO `bus_comment` VALUES (17, 1, 2, 5, '32213', 0, '2026-01-31 16:49:00');
INSERT INTO `bus_comment` VALUES (18, 1, 2, 5, '123', 0, '2026-01-31 17:26:22');
INSERT INTO `bus_comment` VALUES (19, 1, 3, 1, '231', 0, '2026-01-31 17:26:34');

-- ----------------------------
-- Table structure for bus_favorite
-- ----------------------------
DROP TABLE IF EXISTS `bus_favorite`;
CREATE TABLE `bus_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_id` bigint NOT NULL COMMENT '目标ID(商品/帖子ID)',
  `type` int NOT NULL COMMENT '1-商品, 2-帖子',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_target_type`(`target_id` ASC, `type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bus_favorite
-- ----------------------------
INSERT INTO `bus_favorite` VALUES (11, 1, 1, 2, '2026-01-25 22:48:29');
INSERT INTO `bus_favorite` VALUES (15, 1, 1, 4, '2026-01-25 22:49:32');
INSERT INTO `bus_favorite` VALUES (17, 1, 2, 3, '2026-01-26 17:01:23');
INSERT INTO `bus_favorite` VALUES (18, 1, 4, 3, '2026-01-31 16:48:31');

-- ----------------------------
-- Table structure for bus_goods
-- ----------------------------
DROP TABLE IF EXISTS `bus_goods`;
CREATE TABLE `bus_goods`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '发布者ID',
  `area_id` bigint NOT NULL COMMENT '区域ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `price` decimal(10, 2) NOT NULL COMMENT '价格',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片URL',
  `status` int NULL DEFAULT 0 COMMENT '0-上架, 1-交易中, 2-已售出, 3-下架',
  `is_delete` int NULL DEFAULT 0 COMMENT '逻辑删除(0-未删, 1-已删)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览量',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '鍘熶环',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍒嗙被',
  `goods_condition` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鎴愯壊',
  `trading_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浜ゆ槗鏂瑰紡',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浜ゆ槗鍦扮偣',
  `bargain_allowed` tinyint(1) NULL DEFAULT 1 COMMENT '鏄?惁鎺ュ彈璁?环 1-鏄?0-鍚',
  `want_count` int NULL DEFAULT 0 COMMENT '\"我想要\"点击次数',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_area_id`(`area_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bus_goods
-- ----------------------------
INSERT INTO `bus_goods` VALUES (1, 1, 1, '111', '12212', 22.00, '/profile/a3828237-6b6c-4d2f-8632-b446157dfa43.jpg', 0, 1, '2026-01-24 23:37:55', '2026-01-30 22:37:21', 2, 22.00, '电子产品', '全新', NULL, '北京大学', 0, 0);
INSERT INTO `bus_goods` VALUES (2, 1, 1, '213123123', '1312321312', 223.00, '/profile/378efa0e-d3f1-4599-b9e0-f9a3b55aabb3.jpg', 0, 1, '2026-01-25 02:52:56', '2026-01-31 15:03:56', 56, 111.00, '电子产品', '全新', NULL, '浙江大学', 1, 0);
INSERT INTO `bus_goods` VALUES (3, 1, 1, 'A186', '不要了111', 500.00, '/profile/bd9bea37-9250-480f-92b0-42050580ccf6.jpg', 0, 0, '2026-01-31 15:42:19', '2026-01-31 15:42:19', 10, 790.00, '其他', '轻微使用痕迹', NULL, '浙江大学', 1, 0);

-- ----------------------------
-- Table structure for bus_lost_found
-- ----------------------------
DROP TABLE IF EXISTS `bus_lost_found`;
CREATE TABLE `bus_lost_found`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '发布者ID',
  `type` int NOT NULL COMMENT '0-失物, 1-招领',
  `item_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物品名称',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类(证件/电子/日用等)',
  `time` datetime NULL DEFAULT NULL COMMENT '丢失/拾取时间',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地点',
  `latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度',
  `features` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '特征描述',
  `image_urls` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片URLs',
  `contact_info` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系方式',
  `is_public` int NULL DEFAULT 1 COMMENT '是否公开展示联系方式 0-否 1-是',
  `status` int NULL DEFAULT 0 COMMENT '0-发布中 1-已找回/已归还 2-已下架',
  `is_delete` int NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `university` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '失物招领表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bus_lost_found
-- ----------------------------
INSERT INTO `bus_lost_found` VALUES (1, 1, 0, '1222', '证件', '2026-01-24 13:05:00', '121122121', NULL, NULL, '12122112', '', '手机号: 211221122', 1, 0, 1, '2026-01-24 23:53:56', '2026-01-31 15:14:47', '北京大学');
INSERT INTO `bus_lost_found` VALUES (2, 1, 0, '蓝白色内裤', '衣物饰品', '2026-01-30 00:00:00', '123321宿舍', NULL, NULL, '蓝白色的内裤', '/profile/a129ed88-6ecd-4b3a-8db9-c0dcc1abe0f1.jpg', '手机号: 1231312321321312321', 0, 0, 1, '2026-01-25 20:56:51', '2026-01-31 15:14:46', '浙江大学');
INSERT INTO `bus_lost_found` VALUES (3, 1, 0, '内裤', '生活用品', '2026-01-31 00:00:00', '12312312', NULL, NULL, '12312312', '', '手机号: 123123213', 1, 0, 0, '2026-01-31 16:40:44', '2026-01-31 16:40:44', '2131231232');
INSERT INTO `bus_lost_found` VALUES (4, 1, 0, '假几把', '证件', '2026-01-31 00:00:00', 'dadawd', NULL, NULL, '3123213123', '', '手机号: 123123123123123', 1, 0, 0, '2026-01-31 16:48:20', '2026-01-31 16:48:20', '浙江大学');

-- ----------------------------
-- Table structure for bus_order
-- ----------------------------
DROP TABLE IF EXISTS `bus_order`;
CREATE TABLE `bus_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `seller_id` bigint NOT NULL COMMENT '卖家ID',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '成交金额',
  `status` int NULL DEFAULT 0 COMMENT '0-待支付, 1-已支付, 2-已完成, 3-已取消',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bus_order
-- ----------------------------

-- ----------------------------
-- Table structure for bus_post
-- ----------------------------
DROP TABLE IF EXISTS `bus_post`;
CREATE TABLE `bus_post`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '发布者ID',
  `area_id` bigint NOT NULL COMMENT '区域ID',
  `type` int NOT NULL COMMENT '1-失物招领, 3-广场',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `image_urls` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片URLs',
  `is_delete` int NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类/话题',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签',
  `scope` int NULL DEFAULT 0 COMMENT '可见范围: 0-全校, 1-本院, 2-仅好友',
  `is_anonymous` int NULL DEFAULT 0 COMMENT '是否匿名: 0-否, 1-是',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` int NULL DEFAULT 0 COMMENT '状态: 0-正常显示, 1-已下架',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_area_id`(`area_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bus_post
-- ----------------------------
INSERT INTO `bus_post` VALUES (1, 1, 1, 3, '....我喜欢你', '', 1, '2026-01-25 21:03:15', '2026-01-30 23:31:21', '喜欢你', '表白墙', '表白', 0, 0, '浙江大学', 1);
INSERT INTO `bus_post` VALUES (2, 1, 1, 3, '3123123123', '', 0, '2026-01-31 16:48:49', '2026-01-31 16:48:49', '1231231', '日常吐槽', '123', 0, 0, '浙江大学', 0);

-- ----------------------------
-- Table structure for bus_report
-- ----------------------------
DROP TABLE IF EXISTS `bus_report`;
CREATE TABLE `bus_report`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reporter_id` bigint NOT NULL COMMENT '举报人ID',
  `target_type` int NOT NULL COMMENT '被举报对象类型: 1-商品, 2-动态, 3-兼职, 4-失物招领, 5-用户',
  `target_id` bigint NOT NULL COMMENT '被举报对象ID',
  `target_user_id` bigint NULL DEFAULT NULL COMMENT '被举报用户ID',
  `report_type` int NOT NULL COMMENT '举报类型: 1-虚假信息, 2-诈骗, 3-违禁品, 4-色情低俗, 5-骚扰辱骂, 6-其他',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报理由详情',
  `status` int NOT NULL DEFAULT 0 COMMENT '处理状态: 0-待处理, 1-已处理, 2-已驳回',
  `handle_result` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理结果说明',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理人ID',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `is_delete` int NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_reporter_id`(`reporter_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '举报信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bus_report
-- ----------------------------

-- ----------------------------
-- Table structure for part_time_job
-- ----------------------------
DROP TABLE IF EXISTS `part_time_job`;
CREATE TABLE `part_time_job`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `work_time` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `work_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `salary` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `settlement_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `recruit_count` int NULL DEFAULT NULL,
  `student_requirement` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `contact_info` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` int NULL DEFAULT 0,
  `view_count` int NULL DEFAULT 0 COMMENT '浏览量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_delete` int NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of part_time_job
-- ----------------------------
INSERT INTO `part_time_job` VALUES (1, 1, '123321', '333333333213', '', '23123213121', '2222元/天', '完工结', 1, '无限制', NULL, '手机号: 1233212323', 0, 11, '2026-01-25 20:42:44', '2026-01-31 15:14:43', '浙江大学', 1);
INSERT INTO `part_time_job` VALUES (2, 1, '打桩', '打桩', '', '312', '12元/天', '完工结', 1, '无限制', NULL, '手机号: 123123123', 0, 0, '2026-01-31 16:39:00', '2026-01-31 16:39:00', '', 0);
INSERT INTO `part_time_job` VALUES (3, 1, '打桩', '打桩', '2026-01-31 16:39 至 2026-01-31 18:39', '123123', '123123元/天', '完工结', 1, '无限制', NULL, '手机号: 21312312312', 0, 0, '2026-01-31 16:39:38', '2026-01-31 16:39:38', '', 0);
INSERT INTO `part_time_job` VALUES (4, 1, '打桩', '12312312', '2026-01-31 16:46 至 2026-01-31 18:48', '123', '12312元/天', '完工结', 1, '无限制', NULL, '手机号: 123123', 0, 7, '2026-01-31 16:47:10', '2026-01-31 16:47:09', '浙江大学', 0);

-- ----------------------------
-- Table structure for sys_area
-- ----------------------------
DROP TABLE IF EXISTS `sys_area`;
CREATE TABLE `sys_area`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区域名称',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型(school/community)',
  `latitude` decimal(10, 6) NOT NULL COMMENT '纬度',
  `longitude` decimal(10, 6) NOT NULL COMMENT '经度',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '区域表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_area
-- ----------------------------
INSERT INTO `sys_area` VALUES (1, '北京大学', 'school', 39.986913, 116.305874);
INSERT INTO `sys_area` VALUES (2, '天通苑社区', 'community', 40.065547, 116.417384);
INSERT INTO `sys_area` VALUES (3, '北京大学', 'school', 39.986913, 116.305874);
INSERT INTO `sys_area` VALUES (4, '天通苑社区', 'community', 40.065547, 116.417384);
INSERT INTO `sys_area` VALUES (5, '北京大学', 'school', 39.986913, 116.305874);
INSERT INTO `sys_area` VALUES (6, '天通苑社区', 'community', 40.065547, 116.417384);
INSERT INTO `sys_area` VALUES (7, '北京大学', 'school', 39.986913, 116.305874);
INSERT INTO `sys_area` VALUES (8, '天通苑社区', 'community', 40.065547, 116.417384);
INSERT INTO `sys_area` VALUES (9, '北京大学', 'school', 39.986913, 116.305874);
INSERT INTO `sys_area` VALUES (10, '天通苑社区', 'community', 40.065547, 116.417384);

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Notice Title',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Notice Content',
  `type` int NULL DEFAULT 0 COMMENT 'Type: 0-All Users, 1-Specific User',
  `target_id` bigint NULL DEFAULT NULL COMMENT 'Target User ID (if specific)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'System Notice Table' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------

-- ----------------------------
-- Table structure for sys_report
-- ----------------------------
DROP TABLE IF EXISTS `sys_report`;
CREATE TABLE `sys_report`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `reporter_id` bigint NOT NULL COMMENT 'Reporter User ID',
  `target_id` bigint NOT NULL COMMENT 'Target ID (Post/Goods/Comment/User)',
  `type` int NOT NULL COMMENT 'Type: 1-Goods, 2-Post, 3-User, 4-Comment',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Report Reason',
  `status` int NULL DEFAULT 0 COMMENT 'Status: 0-Pending, 1-Handled, 2-Ignored',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_reporter`(`reporter_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Report Management Table' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_report
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '微信OpenID',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '此字段后加上',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'user' COMMENT '角色',
  `status` int NULL DEFAULT 1 COMMENT '1: 正常, 0: 封禁',
  `current_area_id` bigint NULL DEFAULT NULL COMMENT '当前区域ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `bio` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人简介',
  `bg_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人主页背景图',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_openid`(`openid` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'oea6Y7QeYN4ffZ2zS1cGTX9RTGaI', '子衿', '/profile/8d6742ab-c3d7-4390-8723-0c47b851bceb.jpg', NULL, 'admin', 1, NULL, '2026-01-14 02:28:41', '2026-01-31 00:57:36', '1嗯啊', '/profile/42fbe567-d7f6-4341-816d-bb1223e09abf.jpg', 'admin', '123456', NULL);

-- ----------------------------
-- Table structure for user_follows
-- ----------------------------
DROP TABLE IF EXISTS `user_follows`;
CREATE TABLE `user_follows`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `follower_id` bigint NOT NULL COMMENT '关注者ID',
  `followed_id` bigint NOT NULL COMMENT '被关注者ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_follow`(`follower_id` ASC, `followed_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户关注表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_follows
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;

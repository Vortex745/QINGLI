SET NAMES utf8mb4;
-- 插入商品
INSERT INTO bus_goods (
        user_id,
        area_id,
        name,
        description,
        price,
        status,
        is_delete,
        view_count,
        category,
        goods_condition,
        location
    )
VALUES (
        1,
        1,
        'Mate 60 Pro 极简白',
        '九九新，自用纯净，没有任何磕碰。',
        2999.00,
        0,
        0,
        0,
        '电子产品',
        '全新',
        '北京大学'
    );
-- 插入帖子
INSERT INTO bus_post (
        user_id,
        area_id,
        type,
        content,
        title,
        category,
        location
    )
VALUES (
        1,
        1,
        3,
        '今天天气不错，图书馆的空气也格外清新~',
        '美好的一天',
        '校园生活',
        '北京大学图书馆'
    );
-- 插入系统消息
INSERT INTO bus_chat_message (sender_id, receiver_id, content, type, is_read)
VALUES (0, 1, '欢迎来到全新的仙趣社区！我们已经为您清理了环境，快去体验吧。', 9, 0);
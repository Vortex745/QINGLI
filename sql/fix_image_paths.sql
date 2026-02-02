-- 数据库图片路径修复脚本
-- 作用：将数据库中存储的绝对路径 (http://...) 转换为相对路径 (/profile/...)
-- 运行前请务必备份数据库！
-- 1. 修复商品表 (bus_goods) 的图片字段
-- 假设旧 IP 是 192.168.5.14，端口 8081
UPDATE bus_goods
SET image_url = REPLACE(image_url, 'http://192.168.5.14:8081/api', '')
WHERE image_url LIKE '%http://192.168.5.14:8081/api%';
UPDATE bus_goods
SET image_url = REPLACE(image_url, 'http://192.168.5.14:8081', '')
WHERE image_url LIKE '%http://192.168.5.14:8081%'
    AND image_url LIKE '/profile/%';
-- 2. 修复帖子表 (bus_post) 的图片字段
UPDATE bus_post
SET images = REPLACE(
        images,
        'http://192.168.5.14:8081/profile/',
        '/profile/'
    )
WHERE images LIKE '%http://192.168.5.14:8081/profile/%';
-- 3. 修复用户头像 (sys_user)
UPDATE sys_user
SET avatar = REPLACE(
        avatar,
        'http://192.168.5.14:8081/profile/',
        '/profile/'
    )
WHERE avatar LIKE '%http://192.168.5.14:8081/profile/%';
-- Add status column to bus_post table for shelf status functionality
ALTER TABLE bus_post
ADD COLUMN IF NOT EXISTS status INT DEFAULT 0 COMMENT '状态: 0-正常显示, 1-已下架';
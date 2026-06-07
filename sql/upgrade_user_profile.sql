-- 扩展用户表，新增个人资料相关字段
USE smart_elderly;

-- 新增展示名称字段
ALTER TABLE users ADD COLUMN IF NOT EXISTS display_name VARCHAR(100) COMMENT '展示名称' AFTER username;

-- 新增联系电话字段
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20) COMMENT '联系电话' AFTER display_name;

-- 新增最近登录时间字段
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_time DATETIME COMMENT '最近登录时间' AFTER created_at;

-- 为现有用户初始化展示名称（使用用户名）
UPDATE users SET display_name = username WHERE display_name IS NULL OR display_name = '';

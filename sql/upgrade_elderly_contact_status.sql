-- 老人表扩展字段更新脚本
-- 用于在已存在的数据库中添加紧急联系人和状态字段

USE smart_elderly;

-- 添加紧急联系人字段
ALTER TABLE elderly 
ADD COLUMN IF NOT EXISTS emergency_contact_name VARCHAR(50) COMMENT '紧急联系人姓名' AFTER address,
ADD COLUMN IF NOT EXISTS emergency_contact_phone VARCHAR(20) COMMENT '紧急联系人电话' AFTER emergency_contact_name,
ADD COLUMN IF NOT EXISTS emergency_contact_relation VARCHAR(20) COMMENT '紧急联系人关系' AFTER emergency_contact_phone;

-- 添加状态字段
ALTER TABLE elderly 
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT '正常' COMMENT '状态：正常/住院/外出/失联' AFTER emergency_contact_relation;

-- 更新现有数据的状态默认值
UPDATE elderly SET status = '正常' WHERE status IS NULL;

-- 为老人表增加合并相关字段
ALTER TABLE elderly 
ADD COLUMN merged_to_id INT DEFAULT NULL COMMENT '被合并到的主档案ID' AFTER status,
ADD COLUMN merged_at TIMESTAMP NULL COMMENT '合并时间' AFTER merged_to_id,
ADD COLUMN merged_by VARCHAR(50) DEFAULT NULL COMMENT '合并操作人' AFTER merged_at,
ADD INDEX idx_merged_to_id (merged_to_id);

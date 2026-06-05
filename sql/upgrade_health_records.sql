-- 健康记录表扩展字段更新脚本
-- 用于在已存在的数据库中添加新的健康指标字段

USE smart_elderly;

-- 添加新字段
ALTER TABLE health_records 
ADD COLUMN IF NOT EXISTS systolic_pressure INT COMMENT '收缩压' AFTER blood_pressure,
ADD COLUMN IF NOT EXISTS diastolic_pressure INT COMMENT '舒张压' AFTER systolic_pressure,
ADD COLUMN IF NOT EXISTS heart_rate INT COMMENT '心率(次/分钟)' AFTER temperature,
ADD COLUMN IF NOT EXISTS blood_oxygen INT COMMENT '血氧饱和度(%)' AFTER heart_rate,
ADD COLUMN IF NOT EXISTS blood_sugar DECIMAL(4, 2) COMMENT '血糖(mmol/L)' AFTER blood_oxygen,
ADD COLUMN IF NOT EXISTS abnormal_reason VARCHAR(255) COMMENT '异常原因' AFTER blood_sugar;

-- 从现有的 blood_pressure 字段解析收缩压和舒张压
-- 格式为 "120/80" 的字符串
UPDATE health_records 
SET 
    systolic_pressure = CAST(SUBSTRING_INDEX(blood_pressure, '/', 1) AS UNSIGNED),
    diastolic_pressure = CAST(SUBSTRING_INDEX(blood_pressure, '/', -1) AS UNSIGNED)
WHERE blood_pressure IS NOT NULL AND blood_pressure LIKE '%/%';

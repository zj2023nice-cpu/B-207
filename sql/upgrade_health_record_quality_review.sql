-- 健康数据质量复核中心升级脚本

-- 1. 在健康记录表中添加质量状态字段
ALTER TABLE health_records 
ADD COLUMN quality_status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '质量状态：NORMAL-正常，SUSPICIOUS-待复核，REVIEWED-已复核，IGNORED-已忽略' AFTER corrected,
ADD COLUMN quality_score INT DEFAULT 100 COMMENT '质量评分(0-100)' AFTER quality_status,
ADD COLUMN quality_issues TEXT COMMENT '质量问题描述(JSON数组)' AFTER quality_score;

-- 2. 创建质量复核规则表
CREATE TABLE IF NOT EXISTS quality_review_rules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    rule_code VARCHAR(50) NOT NULL UNIQUE COMMENT '规则编码',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_description TEXT COMMENT '规则描述',
    rule_type VARCHAR(30) NOT NULL COMMENT '规则类型：MISSING-缺失检测，EXTREME-极端值检测，DUPLICATE-重复检测，CONTRADICTION-逻辑矛盾',
    severity VARCHAR(20) DEFAULT 'WARNING' COMMENT '严重程度：INFO-提示，WARNING-警告，CRITICAL-严重',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    config_json TEXT COMMENT '规则配置参数(JSON)',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量复核规则表';

-- 3. 创建质量复核记录表
CREATE TABLE IF NOT EXISTS health_record_quality_reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    health_record_id INT NOT NULL COMMENT '健康记录ID',
    elderly_id INT NOT NULL COMMENT '老人ID',
    review_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '复核状态：PENDING-待复核，APPROVED-确认有效，CORRECTED-已更正，IGNORED-忽略',
    triggered_rules TEXT COMMENT '触发的规则编码(JSON数组)',
    issues_summary TEXT COMMENT '问题摘要',
    quality_score INT COMMENT '质量评分',
    reviewer_id INT COMMENT '复核人ID',
    reviewer_name VARCHAR(50) COMMENT '复核人姓名',
    review_time DATETIME COMMENT '复核时间',
    review_conclusion TEXT COMMENT '复核结论',
    ignore_reason TEXT COMMENT '忽略原因',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_health_record_id (health_record_id),
    INDEX idx_elderly_id (elderly_id),
    INDEX idx_review_status (review_status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康记录质量复核表';

-- 4. 初始化默认规则
INSERT INTO quality_review_rules (rule_code, rule_name, rule_description, rule_type, severity, enabled, config_json, sort_order) VALUES
('MISSING_VITAL_SIGNS', '关键生命体征缺失', '检测血压、心率、体温等关键指标是否缺失', 'MISSING', 'WARNING', 1, '{"requiredFields": ["systolicPressure", "diastolicPressure", "heartRate", "temperature"]}', 1),
('EXTREME_BLOOD_PRESSURE', '血压极端值', '检测血压是否超出合理生理范围', 'EXTREME', 'CRITICAL', 1, '{"systolicMin": 50, "systolicMax": 220, "diastolicMin": 30, "diastolicMax": 130}', 2),
('EXTREME_TEMPERATURE', '体温极端值', '检测体温是否超出合理生理范围', 'EXTREME', 'WARNING', 1, '{"tempMin": 34, "tempMax": 43}', 3),
('EXTREME_HEART_RATE', '心率极端值', '检测心率是否超出合理生理范围', 'EXTREME', 'WARNING', 1, '{"hrMin": 30, "hrMax": 220}', 4),
('EXTREME_BLOOD_OXYGEN', '血氧极端值', '检测血氧是否超出合理生理范围', 'EXTREME', 'WARNING', 1, '{"spo2Min": 70, "spo2Max": 100}', 5),
('EXTREME_BLOOD_SUGAR', '血糖极端值', '检测血糖是否超出合理生理范围', 'EXTREME', 'WARNING', 1, '{"glucoseMin": 1, "glucoseMax": 30}', 6),
('QUICK_DUPLICATE', '短时间重复录入', '检测同一老人在短时间内是否有重复录入', 'DUPLICATE', 'WARNING', 1, '{"timeWindowMinutes": 5}', 7),
('BLOOD_PRESSURE_CONTRADICTION', '血压逻辑矛盾', '检测收缩压是否小于等于舒张压', 'CONTRADICTION', 'CRITICAL', 1, '{}', 8),
('ABNORMAL_NO_REASON', '异常无原因说明', '标记为异常但未填写异常原因', 'CONTRADICTION', 'INFO', 1, '{}', 9);

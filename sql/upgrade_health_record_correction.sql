-- ========================================
-- 健康记录更正流程升级脚本
-- ========================================

-- 1. 扩展健康记录表，添加更正相关字段
ALTER TABLE health_records 
ADD COLUMN IF NOT EXISTS corrected BOOLEAN DEFAULT FALSE COMMENT '是否已更正',
ADD COLUMN IF NOT EXISTS corrected_at TIMESTAMP NULL COMMENT '更正时间',
ADD COLUMN IF NOT EXISTS corrected_by VARCHAR(50) NULL COMMENT '更正人',
ADD COLUMN IF NOT EXISTS latest_correction_id INT NULL COMMENT '最新更正记录ID';

-- 2. 创建健康记录更正表
CREATE TABLE IF NOT EXISTS health_record_corrections (
    id INT AUTO_INCREMENT PRIMARY KEY,
    health_record_id INT NOT NULL COMMENT '关联的原始健康记录ID',
    elderly_id INT NOT NULL COMMENT '老人ID（冗余，方便查询）',
    
    -- 修改前快照
    before_blood_pressure VARCHAR(20),
    before_systolic_pressure INT,
    before_diastolic_pressure INT,
    before_temperature DECIMAL(4, 2),
    before_heart_rate INT,
    before_blood_oxygen INT,
    before_blood_sugar DECIMAL(4, 2),
    before_abnormal_reason VARCHAR(255),
    before_is_abnormal BOOLEAN DEFAULT FALSE,
    
    -- 修改后数据
    after_blood_pressure VARCHAR(20),
    after_systolic_pressure INT,
    after_diastolic_pressure INT,
    after_temperature DECIMAL(4, 2),
    after_heart_rate INT,
    after_blood_oxygen INT,
    after_blood_sugar DECIMAL(4, 2),
    after_abnormal_reason VARCHAR(255),
    after_is_abnormal BOOLEAN DEFAULT FALSE,
    
    -- 更正元数据
    correction_reason VARCHAR(500) NOT NULL COMMENT '更正原因',
    corrected_by VARCHAR(50) NOT NULL COMMENT '更正操作人',
    corrected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更正时间',
    correction_remark VARCHAR(500) NULL COMMENT '更正备注',
    
    -- 状态
    status VARCHAR(20) DEFAULT 'EFFECTIVE' COMMENT '状态：EFFECTIVE(已生效), REVERTED(已回滚)',
    version INT DEFAULT 1 COMMENT '更正版本号，第几次更正',
    
    -- 关联
    FOREIGN KEY (health_record_id) REFERENCES health_records(id) ON DELETE CASCADE,
    FOREIGN KEY (elderly_id) REFERENCES elderly(id) ON DELETE CASCADE,
    
    -- 索引
    INDEX idx_health_record_id (health_record_id),
    INDEX idx_elderly_id (elderly_id),
    INDEX idx_corrected_at (corrected_at),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康记录更正表';

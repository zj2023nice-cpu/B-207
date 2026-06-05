-- 健康预警系统数据库初始化脚本
-- 包含阈值配置表、预警记录表、通知表

USE smart_elderly;

-- 健康预警阈值配置表
CREATE TABLE IF NOT EXISTS health_warning_thresholds (
    id INT AUTO_INCREMENT PRIMARY KEY,
    elderly_id INT NULL COMMENT '老人ID，NULL表示系统默认阈值',
    indicator_type VARCHAR(50) NOT NULL COMMENT '指标类型：temperature, systolicPressure, diastolicPressure, heartRate, bloodOxygen, bloodSugar',
    high_threshold DECIMAL(10, 2) NULL COMMENT '高阈值',
    low_threshold DECIMAL(10, 2) NULL COMMENT '低阈值',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_elderly_indicator (elderly_id, indicator_type),
    FOREIGN KEY (elderly_id) REFERENCES elderly(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康预警阈值配置表';

-- 健康预警记录表
CREATE TABLE IF NOT EXISTS health_warning_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    elderly_id INT NOT NULL COMMENT '老人ID',
    health_record_id INT NULL COMMENT '关联的健康记录ID',
    indicator_type VARCHAR(50) NOT NULL COMMENT '指标类型',
    actual_value DECIMAL(10, 2) NOT NULL COMMENT '实际值',
    threshold_value DECIMAL(10, 2) NOT NULL COMMENT '阈值',
    warning_level VARCHAR(20) DEFAULT 'NORMAL' COMMENT '预警级别：LOW(偏低), HIGH(偏高)',
    warning_message VARCHAR(255) COMMENT '预警消息',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING(待处理), READ(已读), HANDLED(已处理)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    handled_at TIMESTAMP NULL COMMENT '处理时间',
    handled_by VARCHAR(50) NULL COMMENT '处理人',
    handle_remark VARCHAR(255) NULL COMMENT '处理备注',
    FOREIGN KEY (elderly_id) REFERENCES elderly(id) ON DELETE CASCADE,
    FOREIGN KEY (health_record_id) REFERENCES health_records(id) ON DELETE SET NULL,
    INDEX idx_elderly_id (elderly_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康预警记录表';

-- 通知表
CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL COMMENT '用户ID',
    elderly_id INT NULL COMMENT '老人ID',
    warning_record_id INT NULL COMMENT '关联的预警记录ID',
    title VARCHAR(100) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    notification_type VARCHAR(50) DEFAULT 'SYSTEM' COMMENT '通知类型：HEALTH_WARNING(健康预警), SYSTEM(系统通知)',
    status VARCHAR(20) DEFAULT 'UNREAD' COMMENT '状态：UNREAD(未读), READ(已读)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL COMMENT '读取时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (elderly_id) REFERENCES elderly(id) ON DELETE SET NULL,
    FOREIGN KEY (warning_record_id) REFERENCES health_warning_records(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 插入系统默认阈值配置
INSERT INTO health_warning_thresholds (elderly_id, indicator_type, high_threshold, low_threshold, enabled) VALUES 
(NULL, 'temperature', 37.3, NULL, TRUE),
(NULL, 'systolicPressure', 140, 90, TRUE),
(NULL, 'diastolicPressure', 90, 60, TRUE),
(NULL, 'heartRate', 100, 60, TRUE),
(NULL, 'bloodOxygen', NULL, 95, TRUE),
(NULL, 'bloodSugar', 6.1, 3.9, TRUE);

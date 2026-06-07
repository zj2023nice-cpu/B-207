SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS smart_elderly DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE smart_elderly;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 老人表
CREATE TABLE IF NOT EXISTS elderly (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    age INT,
    gender VARCHAR(10),
    phone VARCHAR(20),
    address VARCHAR(255),
    emergency_contact_name VARCHAR(50) COMMENT '紧急联系人姓名',
    emergency_contact_phone VARCHAR(20) COMMENT '紧急联系人电话',
    emergency_contact_relation VARCHAR(20) COMMENT '紧急联系人关系',
    status VARCHAR(20) DEFAULT '正常' COMMENT '状态：正常/住院/外出/失联',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 健康记录表（扩展版）
CREATE TABLE IF NOT EXISTS health_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    elderly_id INT NOT NULL,
    blood_pressure VARCHAR(20),
    systolic_pressure INT,
    diastolic_pressure INT,
    temperature DECIMAL(4, 2),
    heart_rate INT,
    blood_oxygen INT,
    blood_sugar DECIMAL(4, 2),
    abnormal_reason VARCHAR(255),
    is_abnormal BOOLEAN DEFAULT FALSE,
    check_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (elderly_id) REFERENCES elderly(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 老人关注关系表
CREATE TABLE IF NOT EXISTS elderly_follow (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '用户ID',
    elderly_id INT NOT NULL COMMENT '老人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    UNIQUE KEY uk_user_elderly (user_id, elderly_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (elderly_id) REFERENCES elderly(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老人关注关系表';

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
    status VARCHAR(20) DEFAULT 'UNREAD' COMMENT '兼容旧数据的状态字段，当前未读以 notification_read_records 为准',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL COMMENT '兼容旧数据的读取时间字段',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (elderly_id) REFERENCES elderly(id) ON DELETE SET NULL,
    FOREIGN KEY (warning_record_id) REFERENCES health_warning_records(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 通知订阅规则表
CREATE TABLE IF NOT EXISTS notification_subscription (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '用户ID',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用订阅规则',
    notification_types TEXT COMMENT '订阅的消息类型，多个用逗号分隔，为空表示全部',
    only_abnormal BOOLEAN DEFAULT FALSE COMMENT '是否仅订阅异常类消息',
    only_followed_elderly BOOLEAN DEFAULT FALSE COMMENT '是否只接收关注老人的通知',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知订阅规则表';

-- 通知已读记录表
CREATE TABLE IF NOT EXISTS notification_read_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    notification_id INT NOT NULL COMMENT '通知ID',
    user_id INT NOT NULL COMMENT '用户ID',
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '读取时间',
    UNIQUE KEY uk_notification_user (notification_id, user_id),
    FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知已读记录表';

-- 初始数据
INSERT INTO users (username, password, role) VALUES ('admin', '123456', 'admin');

INSERT INTO elderly (name, age, gender, phone, address, emergency_contact_name, emergency_contact_phone, emergency_contact_relation, status) VALUES
('张大爷', 75, '男', '13800138001', '朝阳区幸福路1号', '张明', '13900139001', '儿子', '正常'),
('王奶奶', 82, '女', '13800138002', '海淀区安康里10号', '王丽', '13900139002', '女儿', '正常'),
('李爷爷', 68, '男', '13800138003', '西城区平顺街5号', '李刚', '13900139003', '侄子', '正常');

INSERT INTO health_records (elderly_id, blood_pressure, systolic_pressure, diastolic_pressure, temperature, heart_rate, blood_oxygen, blood_sugar, is_abnormal, check_time) VALUES
(1, '120/80', 120, 80, 36.5, 72, 98, 5.2, FALSE, NOW()),
(1, '130/85', 130, 85, 37.5, 85, 96, 5.8, TRUE, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, '118/75', 118, 75, 36.8, 68, 99, 5.0, FALSE, NOW()),
(3, '140/90', 140, 90, 36.2, 75, 97, 6.0, FALSE, NOW());

INSERT INTO health_warning_thresholds (elderly_id, indicator_type, high_threshold, low_threshold, enabled) VALUES
(NULL, 'temperature', 37.3, NULL, TRUE),
(NULL, 'systolicPressure', 140, 90, TRUE),
(NULL, 'diastolicPressure', 90, 60, TRUE),
(NULL, 'heartRate', 100, 60, TRUE),
(NULL, 'bloodOxygen', NULL, 95, TRUE),
(NULL, 'bloodSugar', 6.1, 3.9, TRUE);

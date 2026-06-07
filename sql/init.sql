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

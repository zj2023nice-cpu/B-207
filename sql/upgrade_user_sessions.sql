SET NAMES utf8mb4;
USE smart_elderly;

CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '用户ID',
    session_id VARCHAR(128) NOT NULL COMMENT '会话ID（JSESSIONID或自定义Token）',
    login_ip VARCHAR(50) COMMENT '登录IP地址',
    login_location VARCHAR(100) COMMENT '登录地理位置',
    user_agent VARCHAR(500) COMMENT '用户代理（浏览器/设备信息）',
    device_type VARCHAR(50) COMMENT '设备类型：PC/Mobile/Tablet',
    browser VARCHAR(100) COMMENT '浏览器信息',
    os VARCHAR(100) COMMENT '操作系统',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE(活跃)/EXPIRED(过期)/INVALIDATED(手动失效)/LOGOUT(退出登录)',
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    last_active_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    expire_time TIMESTAMP NULL COMMENT '过期时间',
    invalidated_at TIMESTAMP NULL COMMENT '失效时间',
    invalidated_by INT NULL COMMENT '失效操作人（用户ID）',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_status (status),
    INDEX idx_login_time (login_time),
    UNIQUE KEY uk_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会话记录表';

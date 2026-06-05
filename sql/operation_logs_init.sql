SET NAMES utf8mb4;
USE smart_elderly;

-- 操作审计日志表
CREATE TABLE IF NOT EXISTS operation_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation VARCHAR(100) NOT NULL COMMENT '操作类型',
    method VARCHAR(255) COMMENT '执行的方法名',
    params TEXT COMMENT '请求参数',
    operation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    ip VARCHAR(50) COMMENT '操作IP地址',
    success BOOLEAN DEFAULT TRUE COMMENT '是否成功',
    error_msg TEXT COMMENT '错误信息',
    description VARCHAR(255) COMMENT '操作描述',
    INDEX idx_username (username),
    INDEX idx_operation (operation),
    INDEX idx_operation_time (operation_time),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志表';

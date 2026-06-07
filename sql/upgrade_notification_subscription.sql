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

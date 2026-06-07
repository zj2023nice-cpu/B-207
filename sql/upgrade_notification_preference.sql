-- 消息偏好配置表
CREATE TABLE IF NOT EXISTS notification_preference (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '用户ID',
    enabled_types TEXT COMMENT '在消息中心显示的通知类型，多个用逗号分隔，为空表示全部显示',
    high_priority_types TEXT COMMENT '高优先级提醒的通知类型，多个用逗号分隔',
    do_not_disturb_enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用免打扰',
    do_not_disturb_start VARCHAR(10) DEFAULT '22:00' COMMENT '免打扰开始时间 HH:mm',
    do_not_disturb_end VARCHAR(10) DEFAULT '08:00' COMMENT '免打扰结束时间 HH:mm',
    show_badge_in_dnd BOOLEAN DEFAULT FALSE COMMENT '免打扰期间是否显示角标',
    sound_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用声音提醒',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息偏好配置表';

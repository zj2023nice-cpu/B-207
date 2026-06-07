-- 系统公告表
CREATE TABLE IF NOT EXISTS system_announcements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告正文',
    publisher_id INT NOT NULL COMMENT '发布人ID',
    publisher_name VARCHAR(50) COMMENT '发布人姓名',
    is_pinned BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
    publish_start_time DATETIME NOT NULL COMMENT '发布开始时间',
    publish_end_time DATETIME COMMENT '发布结束时间（为空表示永久有效）',
    status VARCHAR(20) DEFAULT 'PUBLISHED' COMMENT '状态：DRAFT-草稿，PUBLISHED-已发布',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_publish_time (publish_start_time),
    INDEX idx_status (status),
    INDEX idx_is_pinned (is_pinned)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- 公告已读记录表
CREATE TABLE IF NOT EXISTS announcement_read_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    announcement_id INT NOT NULL COMMENT '公告ID',
    user_id INT NOT NULL COMMENT '用户ID',
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '读取时间',
    UNIQUE KEY uk_announcement_user (announcement_id, user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_announcement_id (announcement_id),
    FOREIGN KEY (announcement_id) REFERENCES system_announcements(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告已读记录表';

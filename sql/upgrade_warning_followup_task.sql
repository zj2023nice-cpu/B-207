-- 预警跟进任务升级脚本
USE smart_elderly;

-- 预警跟进任务表
CREATE TABLE IF NOT EXISTS warning_followup_tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    warning_record_id INT NOT NULL COMMENT '关联的预警记录ID',
    elderly_id INT NOT NULL COMMENT '老人ID',
    title VARCHAR(200) NOT NULL COMMENT '任务标题',
    description TEXT COMMENT '任务描述',
    assignee_id INT NULL COMMENT '负责人用户ID',
    assignee_name VARCHAR(50) NULL COMMENT '负责人姓名（冗余，用于展示）',
    deadline DATETIME NOT NULL COMMENT '截止时间',
    priority VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '优先级：LOW(低), MEDIUM(中), HIGH(高)',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING(待处理), IN_PROGRESS(处理中), COMPLETED(已完成), OVERDUE(已逾期), CANCELLED(已取消)',
    remark TEXT COMMENT '处理备注',
    created_by INT NULL COMMENT '创建人用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    reminder_24h_sent BOOLEAN DEFAULT FALSE COMMENT '24小时提醒是否已发送',
    reminder_1h_sent BOOLEAN DEFAULT FALSE COMMENT '1小时提醒是否已发送',
    overdue_reminder_sent BOOLEAN DEFAULT FALSE COMMENT '逾期提醒是否已发送',
    FOREIGN KEY (warning_record_id) REFERENCES health_warning_records(id) ON DELETE CASCADE,
    FOREIGN KEY (elderly_id) REFERENCES elderly(id) ON DELETE CASCADE,
    FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_warning_record_id (warning_record_id),
    INDEX idx_elderly_id (elderly_id),
    INDEX idx_assignee_id (assignee_id),
    INDEX idx_status (status),
    INDEX idx_deadline (deadline),
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警跟进任务表';

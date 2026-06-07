-- 数据导出任务表
CREATE TABLE IF NOT EXISTS export_task (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    export_type VARCHAR(50) NOT NULL COMMENT '导出类型：ELDERLY/HEALTH_RECORD/WARNING_RECORD',
    export_params TEXT COMMENT '导出查询参数JSON',
    export_range_desc VARCHAR(500) COMMENT '导出范围描述',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/COMPLETED/FAILED/CANCELLED',
    file_name VARCHAR(255) COMMENT '导出文件名',
    file_path VARCHAR(500) COMMENT '文件存储路径',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    total_count INT DEFAULT 0 COMMENT '导出总记录数',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    max_retry INT DEFAULT 3 COMMENT '最大重试次数',
    error_message TEXT COMMENT '错误信息',
    created_by INT COMMENT '创建人ID',
    created_by_name VARCHAR(100) COMMENT '创建人姓名',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    started_at TIMESTAMP NULL COMMENT '开始处理时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    INDEX idx_status (status),
    INDEX idx_created_by (created_by),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据导出任务表';

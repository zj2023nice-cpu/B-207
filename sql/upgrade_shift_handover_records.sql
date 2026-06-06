SET NAMES utf8mb4;
USE smart_elderly;

CREATE TABLE IF NOT EXISTS shift_handover_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    handover_person VARCHAR(50) NOT NULL COMMENT '交班人',
    takeover_person VARCHAR(50) NOT NULL COMMENT '接班人',
    handover_time DATETIME NOT NULL COMMENT '交接时间',
    key_elderly TEXT COMMENT '重点关注老人（JSON数组存储老人ID和姓名）',
    pending_warning_summary TEXT COMMENT '待跟进预警摘要',
    remarks TEXT COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交接班记录表';

CREATE TABLE IF NOT EXISTS shift_handover_warning_relations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    handover_record_id INT NOT NULL COMMENT '交接班记录ID',
    warning_record_id INT NOT NULL COMMENT '预警记录ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (handover_record_id) REFERENCES shift_handover_records(id) ON DELETE CASCADE,
    FOREIGN KEY (warning_record_id) REFERENCES health_warning_records(id) ON DELETE CASCADE,
    UNIQUE KEY uk_handover_warning (handover_record_id, warning_record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交接班预警关联表';

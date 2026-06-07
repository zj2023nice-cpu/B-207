-- 健康预警状态机升级脚本
-- 新增状态：IGNORED(已忽略), REOPENED(重新打开), ESCALATED(升级处理)
-- 新增时间线表

USE smart_elderly;

-- 1. 创建预警处理时间线表
CREATE TABLE IF NOT EXISTS health_warning_timeline (
    id INT AUTO_INCREMENT PRIMARY KEY,
    warning_record_id INT NOT NULL COMMENT '预警记录ID',
    action_type VARCHAR(50) NOT NULL COMMENT '动作类型：CREATE, READ, HANDLE, IGNORE, REOPEN, ESCALATE, UPDATE',
    from_status VARCHAR(20) NULL COMMENT '变更前状态',
    to_status VARCHAR(20) NULL COMMENT '变更后状态',
    operator VARCHAR(50) NULL COMMENT '操作人',
    remark VARCHAR(500) NULL COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warning_record_id) REFERENCES health_warning_records(id) ON DELETE CASCADE,
    INDEX idx_warning_record_id (warning_record_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康预警处理时间线';

-- 2. 为已有数据创建初始时间线记录
INSERT INTO health_warning_timeline (warning_record_id, action_type, from_status, to_status, operator, remark, created_at)
SELECT 
    id,
    'CREATE',
    NULL,
    status,
    handled_by,
    CONCAT('系统创建预警，当前状态：', status),
    created_at
FROM health_warning_records
WHERE id NOT IN (SELECT DISTINCT warning_record_id FROM health_warning_timeline);

-- 3. 为已处理的记录追加处理时间线
INSERT INTO health_warning_timeline (warning_record_id, action_type, from_status, to_status, operator, remark, created_at)
SELECT 
    id,
    'HANDLE',
    'PENDING',
    'HANDLED',
    handled_by,
    handle_remark,
    handled_at
FROM health_warning_records
WHERE status = 'HANDLED' 
  AND handled_at IS NOT NULL
  AND id NOT IN (
      SELECT warning_record_id FROM health_warning_timeline WHERE action_type = 'HANDLE'
  );

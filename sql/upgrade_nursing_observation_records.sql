-- 护理观察记录表
CREATE TABLE IF NOT EXISTS nursing_observation_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    elderly_id INT NOT NULL COMMENT '老人ID',
    observation_time TIMESTAMP NOT NULL COMMENT '观察时间',
    observer VARCHAR(50) NOT NULL COMMENT '观察人',
    observation_type VARCHAR(50) NOT NULL COMMENT '观察类型：日常观察/精神状态/饮食情况/排便情况/睡眠情况/其他',
    remark TEXT COMMENT '文本备注',
    need_follow_up BOOLEAN DEFAULT FALSE COMMENT '是否需要后续跟进',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (elderly_id) REFERENCES elderly(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入示例数据
INSERT INTO nursing_observation_records (elderly_id, observation_time, observer, observation_type, remark, need_follow_up) VALUES 
(1, NOW(), '李护士', '日常观察', '老人精神状态良好，饮食正常，无异常情况。', FALSE),
(1, DATE_SUB(NOW(), INTERVAL 1 DAY), '王护士', '睡眠情况', '夜间睡眠质量较好，起夜1次。', FALSE),
(2, NOW(), '张护士', '精神状态', '老人情绪有些低落，建议多陪伴交流。', TRUE),
(3, NOW(), '李护士', '饮食情况', '食欲良好，三餐正常进食。', FALSE);

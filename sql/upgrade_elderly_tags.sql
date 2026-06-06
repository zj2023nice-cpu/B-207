-- 老人标签表
CREATE TABLE IF NOT EXISTS elderly_tag (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    color VARCHAR(20) DEFAULT '#409EFF' COMMENT '标签颜色',
    status VARCHAR(20) DEFAULT '启用' COMMENT '状态：启用/停用',
    sort INT DEFAULT 0 COMMENT '排序',
    remark VARCHAR(255) COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老人标签表';

-- 老人标签关联表
CREATE TABLE IF NOT EXISTS elderly_tag_relation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    elderly_id INT NOT NULL COMMENT '老人ID',
    tag_id INT NOT NULL COMMENT '标签ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (elderly_id) REFERENCES elderly(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES elderly_tag(id) ON DELETE CASCADE,
    UNIQUE KEY uk_elderly_tag (elderly_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老人标签关联表';

-- 初始标签数据
INSERT INTO elderly_tag (name, color, status, sort, remark) VALUES 
('高龄老人', '#F56C6C', '启用', 1, '80岁以上老人'),
('慢性病', '#E6A23C', '启用', 2, '患有慢性病需要特殊关注'),
('独居老人', '#409EFF', '启用', 3, '独自居住的老人'),
('失能老人', '#909399', '启用', 4, '生活不能自理的老人'),
('低保户', '#67C23A', '启用', 5, '享受低保待遇');

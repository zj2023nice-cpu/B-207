SET NAMES utf8mb4;
USE smart_elderly;

CREATE TABLE IF NOT EXISTS visitor_visit_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    visitor_name VARCHAR(50) NOT NULL COMMENT '来访人姓名',
    visitor_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    relation_with_elderly VARCHAR(20) NOT NULL COMMENT '与老人关系',
    elderly_id INT NOT NULL COMMENT '探访对象ID',
    visit_time DATETIME NOT NULL COMMENT '到访时间',
    leave_time DATETIME COMMENT '离开时间',
    status VARCHAR(20) DEFAULT 'VISITING' COMMENT '来访状态：VISITING-来访中，LEFT-已离开',
    remark VARCHAR(255) COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (elderly_id) REFERENCES elderly(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO visitor_visit_records (visitor_name, visitor_phone, relation_with_elderly, elderly_id, visit_time, leave_time, status, remark) VALUES 
('张明', '13900139001', '儿子', 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 'LEFT', '探望父亲，身体状况良好'),
('王丽', '13900139002', '女儿', 2, DATE_SUB(NOW(), INTERVAL 3 HOUR), NULL, 'VISITING', '带了一些生活用品'),
('李刚', '13900139003', '侄子', 3, DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR), 'LEFT', '短暂探望');

CREATE TABLE IF NOT EXISTS notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    sort_no INT NOT NULL DEFAULT 0,
    publisher VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_notice_status_sort (status, sort_no, created_at)
);

INSERT INTO notice (title, content, status, sort_no, publisher, created_at)
VALUES
('平台公告：文明交易倡议', '请在校内公共区域进行线下交易，注意财产安全，避免私下转账风险。', 1, 10, '系统管理员', NOW()),
('平台公告：租赁与捐赠流程更新', '租赁订单支持续租操作；捐赠支持认领申请与进度跟踪。', 1, 8, '系统管理员', NOW())
ON DUPLICATE KEY UPDATE
title = VALUES(title),
content = VALUES(content),
status = VALUES(status),
sort_no = VALUES(sort_no),
publisher = VALUES(publisher);

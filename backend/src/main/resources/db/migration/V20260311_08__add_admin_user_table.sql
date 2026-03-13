CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(64) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_admin_username (username)
);

INSERT INTO admin_user (username, password, nickname, status, created_at)
VALUES ('admin', '$2a$10$x3jRkgE2sGeWjZrHUIbXgOmOLm//eCVjIG/wrfO5Sa4C1DWevwPL2', '系统管理员', 1, NOW())
ON DUPLICATE KEY UPDATE
password = VALUES(password),
nickname = VALUES(nickname),
status = VALUES(status);

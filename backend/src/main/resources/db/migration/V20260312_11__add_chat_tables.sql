CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_a_id BIGINT NOT NULL,
    user_b_id BIGINT NOT NULL,
    last_message VARCHAR(255) NULL,
    last_message_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_chat_pair (user_a_id, user_b_id),
    KEY idx_chat_last_time (last_message_at)
);

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    read_status TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_chat_message_session_time (session_id, created_at),
    KEY idx_chat_message_to_read (to_user_id, read_status)
);

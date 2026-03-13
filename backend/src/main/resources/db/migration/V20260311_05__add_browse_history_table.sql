CREATE TABLE IF NOT EXISTS browse_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    goods_id BIGINT NOT NULL,
    viewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_browse_user_time (user_id, viewed_at DESC)
);

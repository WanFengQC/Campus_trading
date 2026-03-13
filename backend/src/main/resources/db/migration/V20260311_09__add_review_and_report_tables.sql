CREATE TABLE IF NOT EXISTS review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    goods_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    score TINYINT NOT NULL,
    content VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_review_order_reviewer (order_id, reviewer_id),
    KEY idx_review_goods (goods_id),
    KEY idx_review_reviewee (reviewee_id)
);

CREATE TABLE IF NOT EXISTS report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reporter_id BIGINT NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_id BIGINT NOT NULL,
    reason VARCHAR(128) NOT NULL,
    detail VARCHAR(500) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    handle_note VARCHAR(500) NULL,
    handled_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_report_status (status),
    KEY idx_report_target (target_type, target_id),
    KEY idx_report_reporter (reporter_id)
);

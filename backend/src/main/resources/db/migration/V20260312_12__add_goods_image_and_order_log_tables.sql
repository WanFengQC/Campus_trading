CREATE TABLE IF NOT EXISTS goods_image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    goods_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_goods_image_goods_sort (goods_id, sort_no)
);

CREATE TABLE IF NOT EXISTS order_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    operator_user_id BIGINT NOT NULL,
    action VARCHAR(32) NOT NULL,
    from_status VARCHAR(32) NULL,
    to_status VARCHAR(32) NOT NULL,
    note VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_order_log_order_time (order_id, created_at),
    KEY idx_order_log_operator (operator_user_id)
);

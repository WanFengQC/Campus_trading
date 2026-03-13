-- Baseline schema for local initialization and demo data import.

CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    nickname VARCHAR(64) NULL,
    avatar_url VARCHAR(255) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    audit_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED',
    audit_note VARCHAR(255) NULL,
    audit_time DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_username (username)
);

CREATE TABLE IF NOT EXISTS user_auth (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    auth_type VARCHAR(32) NOT NULL,
    auth_key VARCHAR(128) NOT NULL,
    auth_secret VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_auth_type_key (auth_type, auth_key)
);

CREATE TABLE IF NOT EXISTS category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    description TEXT NULL,
    price DECIMAL(10, 2) NOT NULL,
    condition_level VARCHAR(32) NOT NULL,
    contact_info VARCHAR(64) NOT NULL,
    cover_image_url VARCHAR(255) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'OFF_SHELF',
    audit_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    audit_note VARCHAR(255) NULL,
    audit_time DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS goods_image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    goods_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_goods_image_goods_sort (goods_id, sort_no)
);

CREATE TABLE IF NOT EXISTS trade_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    goods_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    buyer_remark VARCHAR(255) NULL,
    meetup_time DATETIME NULL,
    meetup_location VARCHAR(255) NULL,
    meetup_note VARCHAR(255) NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
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

CREATE TABLE IF NOT EXISTS cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    goods_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cart_user_goods (user_id, goods_id)
);

CREATE TABLE IF NOT EXISTS goods_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    goods_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_favorite_user_goods (user_id, goods_id)
);

CREATE TABLE IF NOT EXISTS browse_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    goods_id BIGINT NOT NULL,
    viewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_browse_user_time (user_id, viewed_at DESC)
);

CREATE TABLE IF NOT EXISTS rental_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    description TEXT NULL,
    daily_rent DECIMAL(10, 2) NOT NULL,
    deposit DECIMAL(10, 2) NOT NULL DEFAULT 0,
    contact_info VARCHAR(64) NOT NULL,
    cover_image_url VARCHAR(255) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rental_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    rental_item_id BIGINT NOT NULL,
    renter_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    daily_rent DECIMAL(10, 2) NOT NULL,
    deposit DECIMAL(10, 2) NOT NULL DEFAULT 0,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    renter_remark VARCHAR(255) NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS donation_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    donor_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    description TEXT NULL,
    contact_info VARCHAR(64) NOT NULL,
    pickup_address VARCHAR(255) NULL,
    cover_image_url VARCHAR(255) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS donation_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    donation_item_id BIGINT NOT NULL,
    claimer_id BIGINT NOT NULL,
    donor_id BIGINT NOT NULL,
    claim_remark VARCHAR(255) NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_donation_record_item (donation_item_id),
    KEY idx_donation_record_claimer (claimer_id),
    KEY idx_donation_record_donor (donor_id)
);

CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(64) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_admin_username (username)
);

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

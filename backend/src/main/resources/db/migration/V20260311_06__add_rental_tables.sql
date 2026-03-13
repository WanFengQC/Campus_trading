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

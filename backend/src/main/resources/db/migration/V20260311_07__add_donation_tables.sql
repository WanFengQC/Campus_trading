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

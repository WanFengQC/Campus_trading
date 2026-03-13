ALTER TABLE trade_order
    ADD COLUMN IF NOT EXISTS meetup_time DATETIME NULL AFTER buyer_remark,
    ADD COLUMN IF NOT EXISTS meetup_location VARCHAR(255) NULL AFTER meetup_time,
    ADD COLUMN IF NOT EXISTS meetup_note VARCHAR(255) NULL AFTER meetup_location;

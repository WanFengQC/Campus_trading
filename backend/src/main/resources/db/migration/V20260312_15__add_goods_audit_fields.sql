ALTER TABLE goods
    ADD COLUMN IF NOT EXISTS audit_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' AFTER status,
    ADD COLUMN IF NOT EXISTS audit_note VARCHAR(255) NULL AFTER audit_status,
    ADD COLUMN IF NOT EXISTS audit_time DATETIME NULL AFTER audit_note;

UPDATE goods
SET audit_status = 'APPROVED',
    audit_note = COALESCE(audit_note, '历史数据自动通过'),
    audit_time = COALESCE(audit_time, NOW())
WHERE status = 'ON_SHELF'
  AND (audit_status = 'PENDING' OR audit_status IS NULL OR audit_status = '');

ALTER TABLE goods
    MODIFY COLUMN status VARCHAR(32) NOT NULL DEFAULT 'OFF_SHELF',
    MODIFY COLUMN audit_status VARCHAR(32) NOT NULL DEFAULT 'PENDING';

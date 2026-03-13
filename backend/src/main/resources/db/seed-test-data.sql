-- Demo seed data for Campus Trading
-- Safe to run multiple times: user/auth/category/goods/order/cart use upsert by unique key or fixed id.

SET NAMES utf8mb4;

-- 1) Users (password for all demo accounts: 123456)
INSERT INTO user (username, nickname, avatar_url, status, audit_status, audit_note, audit_time, created_at) VALUES
('20260001', '卖家小张', NULL, 1, 'APPROVED', '种子数据初始化通过', NOW(), NOW()),
('20260002', '买家小李', NULL, 1, 'APPROVED', '种子数据初始化通过', NOW(), NOW()),
('20260003', '买家小王', NULL, 1, 'APPROVED', '种子数据初始化通过', NOW(), NOW())
ON DUPLICATE KEY UPDATE
nickname = VALUES(nickname),
avatar_url = VALUES(avatar_url),
status = VALUES(status),
audit_status = VALUES(audit_status),
audit_note = VALUES(audit_note),
audit_time = VALUES(audit_time);

SET @uid_seller = (SELECT id FROM user WHERE username = '20260001' LIMIT 1);
SET @uid_buyer1 = (SELECT id FROM user WHERE username = '20260002' LIMIT 1);
SET @uid_buyer2 = (SELECT id FROM user WHERE username = '20260003' LIMIT 1);

INSERT INTO user_auth (user_id, auth_type, auth_key, auth_secret, created_at) VALUES
(@uid_seller, 'PASSWORD', '20260001', '$2a$10$x3jRkgE2sGeWjZrHUIbXgOmOLm//eCVjIG/wrfO5Sa4C1DWevwPL2', NOW()),
(@uid_buyer1, 'PASSWORD', '20260002', '$2a$10$x3jRkgE2sGeWjZrHUIbXgOmOLm//eCVjIG/wrfO5Sa4C1DWevwPL2', NOW()),
(@uid_buyer2, 'PASSWORD', '20260003', '$2a$10$x3jRkgE2sGeWjZrHUIbXgOmOLm//eCVjIG/wrfO5Sa4C1DWevwPL2', NOW())
ON DUPLICATE KEY UPDATE
user_id = VALUES(user_id),
auth_secret = VALUES(auth_secret);

-- 2) Categories
INSERT INTO category (id, name, sort_no, status) VALUES
(60001, '数码电子', 10, 1),
(60002, '图书教材', 20, 1),
(60003, '生活用品', 30, 1),
(60004, '运动户外', 40, 1)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
sort_no = VALUES(sort_no),
status = VALUES(status);

-- 3) Goods
INSERT INTO goods (
    id, seller_id, category_id, title, description, price, condition_level, contact_info, cover_image_url, status, audit_status, audit_note, audit_time, created_at
) VALUES
(70001, @uid_seller, 60001, 'MacBook Air M1 8+256', '正常使用痕迹，电池健康良好，含原装充电器。', 3999.00, '八成新', '微信：campus_seller_01', NULL, 'OFF_SHELF', 'PENDING', '待管理员审核', NULL, NOW() - INTERVAL 10 DAY),
(70002, @uid_seller, 60002, '高等数学（同济第七版）', '笔记较少，书页完整，无缺页。', 35.00, '九成新', '微信：campus_seller_01', NULL, 'OFF_SHELF', 'REJECTED', '封面图与标题不一致，请补充后重提', NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 8 DAY),
(70003, @uid_seller, 60004, '折叠自行车', '可正常骑行，适合校园代步。', 420.00, '七成新及以下', '微信：campus_seller_01', NULL, 'OFF_SHELF', 'PENDING', '待管理员审核', NULL, NOW() - INTERVAL 6 DAY),
(70004, @uid_seller, 60003, '米家台灯 Pro', '亮度可调，灯臂稳定。', 89.00, '九成新', '微信：campus_seller_01', NULL, 'ON_SHELF', 'APPROVED', '审核通过', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
(70005, @uid_seller, 60002, '考研英语真题（2010-2025）', '含部分做题笔记，资料齐全。', 58.00, '八成新', '微信：campus_seller_01', NULL, 'ON_SHELF', 'APPROVED', '审核通过', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY)
ON DUPLICATE KEY UPDATE
seller_id = VALUES(seller_id),
category_id = VALUES(category_id),
title = VALUES(title),
description = VALUES(description),
price = VALUES(price),
condition_level = VALUES(condition_level),
contact_info = VALUES(contact_info),
cover_image_url = VALUES(cover_image_url),
status = VALUES(status),
audit_status = VALUES(audit_status),
audit_note = VALUES(audit_note),
audit_time = VALUES(audit_time);

-- 4) Orders (fixed demo records)
INSERT INTO trade_order (
    id, order_no, goods_id, buyer_id, seller_id, amount, buyer_remark,
    meetup_time, meetup_location, meetup_note, status, created_at
) VALUES
(80001, 'ODDEMO80001', 70001, @uid_buyer1, @uid_seller, 3999.00, '今晚 19:00 线下交易', NOW() - INTERVAL 1 DAY, '图书馆南门', '请携带充电器现场验货', 'PENDING', NOW() - INTERVAL 3 DAY),
(80002, 'ODDEMO80002', 70002, @uid_buyer2, @uid_seller, 35.00, '中午当面交易', NOW() + INTERVAL 2 HOUR, '一食堂门口', '下课后当面交易', 'SELLER_CONFIRMED', NOW() - INTERVAL 2 DAY),
(80003, 'ODDEMO80003', 70003, @uid_buyer1, @uid_seller, 420.00, '已当面完成交易', NOW() - INTERVAL 1 DAY, '体育馆门口', '线下验货完成并成交', 'COMPLETED', NOW() - INTERVAL 1 DAY)
ON DUPLICATE KEY UPDATE
order_no = VALUES(order_no),
goods_id = VALUES(goods_id),
buyer_id = VALUES(buyer_id),
seller_id = VALUES(seller_id),
amount = VALUES(amount),
buyer_remark = VALUES(buyer_remark),
meetup_time = VALUES(meetup_time),
meetup_location = VALUES(meetup_location),
meetup_note = VALUES(meetup_note),
status = VALUES(status);

-- 4.1) Order logs
INSERT INTO order_log (
    id, order_id, operator_user_id, action, from_status, to_status, note, created_at
) VALUES
(80501, 80001, @uid_buyer1, 'CREATE', NULL, 'PENDING', '买家提交订单', NOW() - INTERVAL 3 DAY),
(80502, 80002, @uid_buyer2, 'CREATE', NULL, 'PENDING', '买家提交订单', NOW() - INTERVAL 2 DAY),
(80503, 80002, @uid_seller, 'SELLER_CONFIRM', 'PENDING', 'SELLER_CONFIRMED', '卖家确认线下交易', NOW() - INTERVAL 2 DAY + INTERVAL 30 MINUTE),
(80504, 80003, @uid_buyer1, 'CREATE', NULL, 'PENDING', '买家提交订单', NOW() - INTERVAL 1 DAY),
(80505, 80003, @uid_seller, 'SELLER_CONFIRM', 'PENDING', 'SELLER_CONFIRMED', '卖家确认线下交易', NOW() - INTERVAL 23 HOUR),
(80506, 80003, @uid_buyer1, 'BUYER_COMPLETE', 'SELLER_CONFIRMED', 'COMPLETED', '买家确认交易完成', NOW() - INTERVAL 22 HOUR),
(80507, 80002, @uid_buyer2, 'MEETUP_UPDATE', 'SELLER_CONFIRMED', 'SELLER_CONFIRMED', '更新见面时间和地点', NOW() - INTERVAL 1 HOUR)
ON DUPLICATE KEY UPDATE
order_id = VALUES(order_id),
operator_user_id = VALUES(operator_user_id),
action = VALUES(action),
from_status = VALUES(from_status),
to_status = VALUES(to_status),
note = VALUES(note),
created_at = VALUES(created_at);

-- 5) Cart
INSERT INTO cart (id, user_id, goods_id, created_at) VALUES
(81001, @uid_buyer1, 70004, NOW() - INTERVAL 3 HOUR),
(81002, @uid_buyer1, 70005, NOW() - INTERVAL 1 HOUR)
ON DUPLICATE KEY UPDATE
user_id = VALUES(user_id),
goods_id = VALUES(goods_id);

-- 6) Favorites
INSERT INTO goods_favorite (id, user_id, goods_id, created_at) VALUES
(82001, @uid_buyer1, 70004, NOW() - INTERVAL 2 HOUR),
(82002, @uid_buyer2, 70005, NOW() - INTERVAL 30 MINUTE)
ON DUPLICATE KEY UPDATE
user_id = VALUES(user_id),
goods_id = VALUES(goods_id);

-- 7) Browse history
INSERT INTO browse_history (id, user_id, goods_id, viewed_at) VALUES
(83001, @uid_buyer1, 70005, NOW() - INTERVAL 20 MINUTE),
(83002, @uid_buyer1, 70004, NOW() - INTERVAL 10 MINUTE),
(83003, @uid_buyer2, 70001, NOW() - INTERVAL 5 MINUTE)
ON DUPLICATE KEY UPDATE
user_id = VALUES(user_id),
goods_id = VALUES(goods_id),
viewed_at = VALUES(viewed_at);

-- 8) Rental items
INSERT INTO rental_item (
    id, owner_id, category_id, title, description, daily_rent, deposit, contact_info, cover_image_url, status, created_at
) VALUES
(84001, @uid_seller, 60001, '投影仪（课堂演示用）', '可用于课程展示，支持 HDMI 接口。', 35.00, 200.00, '微信：campus_seller_01', NULL, 'AVAILABLE', NOW() - INTERVAL 4 DAY),
(84002, @uid_seller, 60003, '露营帐篷（双人）', '周末短租，配套地钉齐全。', 28.00, 150.00, '微信：campus_seller_01', NULL, 'RENTING', NOW() - INTERVAL 3 DAY)
ON DUPLICATE KEY UPDATE
owner_id = VALUES(owner_id),
category_id = VALUES(category_id),
title = VALUES(title),
description = VALUES(description),
daily_rent = VALUES(daily_rent),
deposit = VALUES(deposit),
contact_info = VALUES(contact_info),
cover_image_url = VALUES(cover_image_url),
status = VALUES(status);

-- 9) Rental orders
INSERT INTO rental_order (
    id, order_no, rental_item_id, renter_id, owner_id, daily_rent, deposit, start_date, end_date, total_amount, renter_remark, status, created_at
) VALUES
(85001, 'RODEMO85001', 84002, @uid_buyer1, @uid_seller, 28.00, 150.00, CURDATE() - INTERVAL 1 DAY, CURDATE() + INTERVAL 2 DAY, 262.00, '周末社团活动使用。', 'ACTIVE', NOW() - INTERVAL 1 DAY)
ON DUPLICATE KEY UPDATE
order_no = VALUES(order_no),
rental_item_id = VALUES(rental_item_id),
renter_id = VALUES(renter_id),
owner_id = VALUES(owner_id),
daily_rent = VALUES(daily_rent),
deposit = VALUES(deposit),
start_date = VALUES(start_date),
end_date = VALUES(end_date),
total_amount = VALUES(total_amount),
renter_remark = VALUES(renter_remark),
status = VALUES(status);

-- 10) Donation items
INSERT INTO donation_item (
    id, donor_id, category_id, title, description, contact_info, pickup_address, cover_image_url, status, created_at
) VALUES
(86001, @uid_seller, 60002, '《数据结构》教材（可捐）', '书页完整，少量笔记，适合课程学习。', '微信：campus_seller_01', '三教门口', NULL, 'AVAILABLE', NOW() - INTERVAL 2 DAY),
(86002, @uid_seller, 60003, '宿舍收纳箱（两个）', '可正常使用，已清洁。', '微信：campus_seller_01', '二食堂东门', NULL, 'CLAIMED', NOW() - INTERVAL 4 DAY),
(86003, @uid_seller, 60004, '羽毛球拍（初学者）', '球拍完好，赠送拍套。', '微信：campus_seller_01', '操场看台', NULL, 'CLAIM_PENDING', NOW() - INTERVAL 1 DAY)
ON DUPLICATE KEY UPDATE
donor_id = VALUES(donor_id),
category_id = VALUES(category_id),
title = VALUES(title),
description = VALUES(description),
contact_info = VALUES(contact_info),
pickup_address = VALUES(pickup_address),
cover_image_url = VALUES(cover_image_url),
status = VALUES(status);

-- 11) Donation records
INSERT INTO donation_record (
    id, donation_item_id, claimer_id, donor_id, claim_remark, status, created_at
) VALUES
(87001, 86002, @uid_buyer2, @uid_seller, '用于毕业设计宿舍整理。', 'COMPLETED', NOW() - INTERVAL 3 DAY),
(87002, 86003, @uid_buyer1, @uid_seller, '社团活动急需器材。', 'PENDING', NOW() - INTERVAL 6 HOUR)
ON DUPLICATE KEY UPDATE
donation_item_id = VALUES(donation_item_id),
claimer_id = VALUES(claimer_id),
donor_id = VALUES(donor_id),
claim_remark = VALUES(claim_remark),
status = VALUES(status);

-- 12) Admin user
INSERT INTO admin_user (username, password, nickname, status, created_at)
VALUES
('admin', '$2a$10$x3jRkgE2sGeWjZrHUIbXgOmOLm//eCVjIG/wrfO5Sa4C1DWevwPL2', '系统管理员', 1, NOW())
ON DUPLICATE KEY UPDATE
password = VALUES(password),
nickname = VALUES(nickname),
status = VALUES(status);

-- 13) Reviews
INSERT INTO review (
    id, order_id, goods_id, reviewer_id, reviewee_id, score, content, created_at
) VALUES
(88001, 80003, 70003, @uid_buyer1, @uid_seller, 5, '当面交易顺利，沟通及时。', NOW() - INTERVAL 20 HOUR),
(88002, 80003, 70003, @uid_seller, @uid_buyer1, 5, '买家守时，交易体验很好。', NOW() - INTERVAL 18 HOUR)
ON DUPLICATE KEY UPDATE
order_id = VALUES(order_id),
goods_id = VALUES(goods_id),
reviewer_id = VALUES(reviewer_id),
reviewee_id = VALUES(reviewee_id),
score = VALUES(score),
content = VALUES(content);

-- 14) Reports
INSERT INTO report (
    id, reporter_id, target_type, target_id, reason, detail, status, handle_note, handled_at, created_at
) VALUES
(89001, @uid_buyer2, 'GOODS', 70004, '疑似标题与描述不一致', '商品参数描述与实物图片不一致，建议核实。', 'PENDING', NULL, NULL, NOW() - INTERVAL 2 HOUR),
(89002, @uid_buyer1, 'ORDER', 80002, '线下沟通态度问题', '希望平台关注交易沟通规范。', 'RESOLVED', '已联系双方并给出交易规范提醒。', NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 8 HOUR)
ON DUPLICATE KEY UPDATE
reporter_id = VALUES(reporter_id),
target_type = VALUES(target_type),
target_id = VALUES(target_id),
reason = VALUES(reason),
detail = VALUES(detail),
status = VALUES(status),
handle_note = VALUES(handle_note),
handled_at = VALUES(handled_at);

-- 15) Notices
INSERT INTO notice (
    id, title, content, status, sort_no, publisher, created_at
) VALUES
(90001, '平台公告：交易安全提醒', '请优先选择校内公共区域当面交易，谨防私下转账诈骗。', 1, 20, '系统管理员', NOW() - INTERVAL 1 DAY),
(90002, '平台公告：功能更新', '已上线租赁、捐赠、评价、举报模块，欢迎体验并反馈建议。', 1, 15, '系统管理员', NOW() - INTERVAL 12 HOUR)
ON DUPLICATE KEY UPDATE
title = VALUES(title),
content = VALUES(content),
status = VALUES(status),
sort_no = VALUES(sort_no),
publisher = VALUES(publisher);

-- 16) Chat session
SET @chat_u1 = LEAST(@uid_seller, @uid_buyer1);
SET @chat_u2 = GREATEST(@uid_seller, @uid_buyer1);

INSERT INTO chat_session (
    id, user_a_id, user_b_id, last_message, last_message_at, created_at
) VALUES
(91001, @chat_u1, @chat_u2, '好的，今晚 7 点图书馆门口见。', NOW() - INTERVAL 30 MINUTE, NOW() - INTERVAL 1 DAY)
ON DUPLICATE KEY UPDATE
user_a_id = VALUES(user_a_id),
user_b_id = VALUES(user_b_id),
last_message = VALUES(last_message),
last_message_at = VALUES(last_message_at);

-- 17) Chat messages
INSERT INTO chat_message (
    id, session_id, from_user_id, to_user_id, content, read_status, created_at
) VALUES
(92001, 91001, @uid_buyer1, @uid_seller, '你好，这辆自行车还在吗？', 1, NOW() - INTERVAL 2 HOUR),
(92002, 91001, @uid_seller, @uid_buyer1, '在的，可以线下看货。', 1, NOW() - INTERVAL 90 MINUTE),
(92003, 91001, @uid_buyer1, @uid_seller, '好的，今晚 7 点图书馆门口见。', 0, NOW() - INTERVAL 30 MINUTE)
ON DUPLICATE KEY UPDATE
session_id = VALUES(session_id),
from_user_id = VALUES(from_user_id),
to_user_id = VALUES(to_user_id),
content = VALUES(content),
read_status = VALUES(read_status),
created_at = VALUES(created_at);

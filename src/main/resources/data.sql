-- Додавання адміністратора (Логін: admin / Пароль: password)
INSERT INTO users (id, username, first_name, last_name, email, password, role, active, balance) VALUES
    ('f3e02ce0-365d-4c03-90a1-98f00cf6d3d1', 'admin', 'Ivan', 'Adminov', 'admin@mail.com',
     '$2a$12$l8r72cdCsTYhbh5TS5nL9ef7Py7gYp2kU6NIcKnli0ycE1uNfbVQi',
     'ADMIN', true, 10000.00);

-- Додавання менеджера (Логін: manager / Пароль: password)
INSERT INTO users (id, username, first_name, last_name, email, password, role, active, balance) VALUES
    ('e2d02ce0-365d-4c03-90a1-98f00cf6d3d2', 'manager', 'Petro', 'Managerov', 'manager@mail.com',
     '$2a$12$l8r72cdCsTYhbh5TS5nL9ef7Py7gYp2kU6NIcKnli0ycE1uNfbVQi',
     'MANAGER', true, 5000.00);

INSERT INTO users (id, username, first_name, last_name, email, password, role, active, balance) VALUES
    ('e2d02ce0-365d-4c03-90a1-98f00cf6d3d3', 'aaa', 'Petro', 'Managerov', 'a@mail.com',
     '$2a$12$l8r72cdCsTYhbh5TS5nL9ef7Py7gYp2kU6NIcKnli0ycE1uNfbVQi',
     'USER', true, 5000.00);

-- Додавання 30 турів
INSERT INTO vouchers (id, title, description, price, tour_type, transfer_type, hotel_type, status, arrival_date, eviction_date, is_hot) VALUES
-- Гарячі тури (is_hot = true)
('c0000000-0000-0000-0000-000000000001', 'tour.paris.title', 'tour.paris.desc', 1200.00, 'LEISURE', 'PLANE', 'FOUR_STARS', 'REGISTERED', '2024-08-01', '2024-08-04', true),
('c0000000-0000-0000-0000-000000000002', 'tour.milan.title', 'tour.milan.desc', 950.00, 'LEISURE', 'PLANE', 'THREE_STARS', 'REGISTERED', '2024-08-10', '2024-08-15', true),
('c0000000-0000-0000-0000-000000000003', 'tour.rome.title', 'tour.rome.desc', 800.00, 'CULTURAL', 'PLANE', 'THREE_STARS', 'REGISTERED', '2024-08-12', '2024-08-18', true),
('c0000000-0000-0000-0000-000000000004', 'tour.cyprus.title', 'tour.cyprus.desc', 2100.00, 'LEISURE', 'SHIP', 'FIVE_STARS', 'REGISTERED', '2024-08-20', '2024-08-30', true),
('c0000000-0000-0000-0000-000000000005', 'tour.london.title', 'tour.london.desc', 1500.00, 'CULTURAL', 'PLANE', 'FOUR_STARS', 'REGISTERED', '2024-09-01', '2024-09-07', true),

-- Звичайні тури (LEISURE)
('c0000000-0000-0000-0000-000000000006', 'tour.maldives.title', 'tour.maldives.desc', 3200.00, 'LEISURE', 'PLANE', 'FIVE_STARS', 'REGISTERED', '2024-09-10', '2024-09-24', false),
('c0000000-0000-0000-0000-000000000007', 'tour.turkey.title', 'tour.turkey.desc', 1100.00, 'LEISURE', 'PLANE', 'FIVE_STARS', 'REGISTERED', '2024-09-15', '2024-09-22', false),
('c0000000-0000-0000-0000-000000000008', 'tour.egypt.title', 'tour.egypt.desc', 900.00, 'LEISURE', 'PLANE', 'FOUR_STARS', 'REGISTERED', '2024-09-20', '2024-09-27', false),
('c0000000-0000-0000-0000-000000000009', 'tour.greece.title', 'tour.greece.desc', 1300.00, 'LEISURE', 'SHIP', 'FOUR_STARS', 'REGISTERED', '2024-09-25', '2024-10-02', false),
('c0000000-0000-0000-0000-000000000010', 'tour.spain.title', 'tour.spain.desc', 1400.00, 'LEISURE', 'PLANE', 'FOUR_STARS', 'REGISTERED', '2024-10-01', '2024-10-10', false),
('c0000000-0000-0000-0000-000000000011', 'tour.bali.title', 'tour.bali.desc', 4500.00, 'LEISURE', 'PLANE', 'FIVE_STARS', 'REGISTERED', '2024-10-10', '2024-10-24', false),
('c0000000-0000-0000-0000-000000000012', 'tour.caribbean.title', 'tour.caribbean.desc', 3800.00, 'LEISURE', 'SHIP', 'FIVE_STARS', 'REGISTERED', '2024-11-01', '2024-11-15', false),
('c0000000-0000-0000-0000-000000000013', 'tour.bulgaria.title', 'tour.bulgaria.desc', 700.00, 'LEISURE', 'PRIVATE_CAR', 'THREE_STARS', 'REGISTERED', '2024-08-05', '2024-08-12', false),
('c0000000-0000-0000-0000-000000000014', 'tour.montenegro.title', 'tour.montenegro.desc', 650.00, 'LEISURE', 'PRIVATE_CAR', 'THREE_STARS', 'REGISTERED', '2024-08-15', '2024-08-22', false),
('c0000000-0000-0000-0000-000000000015', 'tour.croatia.title', 'tour.croatia.desc', 850.00, 'LEISURE', 'PRIVATE_CAR', 'FOUR_STARS', 'REGISTERED', '2024-09-05', '2024-09-12', false),

-- Звичайні тури (CULTURAL / SAFARI / ECO)
('c0000000-0000-0000-0000-000000000016', 'tour.venice.title', 'tour.venice.desc', 1250.00, 'CULTURAL', 'PLANE', 'FOUR_STARS', 'REGISTERED', '2024-09-10', '2024-09-15', false),
('c0000000-0000-0000-0000-000000000017', 'tour.berlin.title', 'tour.berlin.desc', 950.00, 'CULTURAL', 'PLANE', 'THREE_STARS', 'REGISTERED', '2024-09-18', '2024-09-22', false),
('c0000000-0000-0000-0000-000000000018', 'tour.krakow.title', 'tour.krakow.desc', 400.00, 'CULTURAL', 'PRIVATE_CAR', 'THREE_STARS', 'REGISTERED', '2024-09-25', '2024-09-28', false),
('c0000000-0000-0000-0000-000000000019', 'tour.prague.title', 'tour.prague.desc', 450.00, 'CULTURAL', 'PRIVATE_CAR', 'THREE_STARS', 'REGISTERED', '2024-10-05', '2024-10-09', false),
('c0000000-0000-0000-0000-000000000020', 'tour.athens.title', 'tour.athens.desc', 1150.00, 'CULTURAL', 'PLANE', 'FOUR_STARS', 'REGISTERED', '2024-10-15', '2024-10-21', false),
('c0000000-0000-0000-0000-000000000021', 'tour.vienna.title', 'tour.vienna.desc', 850.00, 'CULTURAL', 'PRIVATE_CAR', 'FOUR_STARS', 'REGISTERED', '2024-10-25', '2024-10-30', false),
('c0000000-0000-0000-0000-000000000022', 'tour.kyiv.title', 'tour.kyiv.desc', 300.00, 'CULTURAL', 'PRIVATE_CAR', 'FIVE_STARS', 'REGISTERED', '2024-11-05', '2024-11-08', false),
('c0000000-0000-0000-0000-000000000023', 'tour.africa.title', 'tour.africa.desc', 2500.00, 'SAFARI', 'JEEPS', 'THREE_STARS', 'REGISTERED', '2024-09-10', '2024-09-20', false),
('c0000000-0000-0000-0000-000000000024', 'tour.carpathians.title', 'tour.carpathians.desc', 350.00, 'ECO', 'PRIVATE_CAR', 'THREE_STARS', 'REGISTERED', '2024-11-20', '2024-11-25', false),

-- Звичайні тури (LEISURE - Shopping themed)
('c0000000-0000-0000-0000-000000000025', 'tour.dubai.title', 'tour.dubai.desc', 2500.00, 'LEISURE', 'PLANE', 'FIVE_STARS', 'REGISTERED', '2024-11-25', '2024-12-05', false),
('c0000000-0000-0000-0000-000000000026', 'tour.newyork.title', 'tour.newyork.desc', 3000.00, 'LEISURE', 'PLANE', 'FOUR_STARS', 'REGISTERED', '2024-11-20', '2024-11-28', false),
('c0000000-0000-0000-0000-000000000027', 'tour.istanbul.title', 'tour.istanbul.desc', 800.00, 'LEISURE', 'PLANE', 'FOUR_STARS', 'REGISTERED', '2024-12-01', '2024-12-05', false),
('c0000000-0000-0000-0000-000000000028', 'tour.warsaw.title', 'tour.warsaw.desc', 200.00, 'LEISURE', 'PRIVATE_CAR', 'THREE_STARS', 'REGISTERED', '2024-12-10', '2024-12-12', false),
('c0000000-0000-0000-0000-000000000029', 'tour.seoul.title', 'tour.seoul.desc', 1800.00, 'LEISURE', 'PLANE', 'FOUR_STARS', 'REGISTERED', '2024-12-15', '2024-12-22', false),
('c0000000-0000-0000-0000-000000000030', 'tour.tokyo.title', 'tour.tokyo.desc', 2900.00, 'LEISURE', 'PLANE', 'FIVE_STARS', 'REGISTERED', '2024-12-20', '2024-12-28', false);
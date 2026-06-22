-- Додавання адміністратора
INSERT INTO users (id, username, first_name, last_name, email, password, role, active) VALUES
    ('f3e02ce0-365d-4c03-90a1-98f00cf6d3d1', 'admin', 'Ivan', 'Adminov', 'admin@mail.com',
     '$2a$12$lGMyH/q2tkSYyDQ4M6Bp8ObuzHAkrPqDBi/.gp8JWmYjRjRCLRwkW',
     'ADMIN', true);

-- Додавання турів
INSERT INTO vouchers (id, title, description, price, tour_type, transfer_type, hotel_type, status, arrival_date, eviction_date, is_hot) VALUES
    ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'Paris Weekend', 'Romantic trip to Paris', 1200.00, 'LEISURE', 'PLANE', 'FOUR_STARS', 'REGISTERED', '2024-08-01', '2024-08-04', true),
    ('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 'African Safari', 'Extreme adventure', 2500.00, 'SAFARI', 'JEEPS', 'THREE_STARS', 'REGISTERED', '2024-09-10', '2024-09-20', false);
ALTER TABLE categories ADD COLUMN system_code VARCHAR(40);
CREATE UNIQUE INDEX uk_categories_system_code ON categories (system_code);

UPDATE categories SET system_code = 'BOOKING' WHERE name = 'Бронирование';

INSERT INTO categories (name, description, system_code)
SELECT 'Бронирование', 'Изменения состояния билета', 'BOOKING'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE system_code = 'BOOKING');

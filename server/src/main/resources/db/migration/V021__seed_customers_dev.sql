-- Dev seed user: test@example.com / 123456
USE flowershop;

INSERT INTO customers (email, password_hash, display_name, phone, is_active)
VALUES ('test@example.com', UPPER(SHA2('123456', 256)), 'Test User', NULL, TRUE) AS new
ON DUPLICATE KEY UPDATE
                     password_hash = new.password_hash,
                     display_name  = new.display_name,
                     phone         = new.phone,
                     is_active     = new.is_active;

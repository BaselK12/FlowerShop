-- 1) Trim legacy CHAR padding
UPDATE customers SET password_hash = RTRIM(password_hash);

-- 2) Normalize emails
UPDATE customers SET email = LOWER(TRIM(email));

-- 3) Email column sizing + collation (MySQL 8)
ALTER TABLE customers
    MODIFY COLUMN email VARCHAR(254)
        CHARACTER SET utf8mb4
        COLLATE utf8mb4_0900_ai_ci
        NOT NULL;

-- If your MySQL is older than 8.0.13 and whines about that collation,
-- swap the line above with utf8mb4_general_ci instead.

-- 4) is_active without deprecated display width
ALTER TABLE customers
    MODIFY COLUMN is_active TINYINT NOT NULL DEFAULT 1;

-- 5) created_at default
ALTER TABLE customers
    MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- 6) Add UNIQUE constraint on email IF MISSING using dynamic SQL
SET @idx_exists := (
    SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'customers'
      AND INDEX_NAME = 'uk_customers_email'
);
SET @sql := IF(@idx_exists = 0,
               'ALTER TABLE customers ADD CONSTRAINT uk_customers_email UNIQUE (email)',
               'SELECT 1'
            );
PREPARE s FROM @sql;
EXECUTE s;
DEALLOCATE PREPARE s;

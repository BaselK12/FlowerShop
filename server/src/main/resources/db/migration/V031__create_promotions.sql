USE flowershop;

CREATE TABLE IF NOT EXISTS promotions (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    description   TEXT,
    discount_type VARCHAR(10)  NOT NULL,  -- 'PERCENT' or 'FIXED'
    amount        DOUBLE       NOT NULL,
    valid_from    DATE NULL,
    valid_to      DATE NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
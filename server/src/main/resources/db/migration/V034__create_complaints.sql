USE flowershop;

CREATE TABLE IF NOT EXISTS complaints (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    category   VARCHAR(100) NOT NULL,
    order_id   VARCHAR(64)  NULL,
    subject    VARCHAR(150) NOT NULL,
    message    TEXT         NOT NULL,
    anonymous  TINYINT(1)   NOT NULL DEFAULT 0,
    email      VARCHAR(254) NULL,
    phone      VARCHAR(32)  NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

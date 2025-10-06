USE flowershop;

CREATE TABLE IF NOT EXISTS employees (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    gender        VARCHAR(10)  NULL,  -- matches EnumType.STRING for Gender
    email         VARCHAR(254) NOT NULL,
    phone         VARCHAR(32)  NULL,
    role          VARCHAR(50)  NOT NULL, -- matches EnumType.STRING for EmployeeRole
    active        TINYINT(1)   NOT NULL DEFAULT 1,
    salary        BIGINT       NOT NULL,
    hire_date     DATE         NULL,
    password_hash CHAR(64)     NULL,
    UNIQUE KEY uk_employees_email (email)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

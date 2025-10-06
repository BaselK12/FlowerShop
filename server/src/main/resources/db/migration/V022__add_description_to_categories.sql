USE flowershop;

ALTER TABLE categories
    ADD COLUMN display_name VARCHAR(255) NULL;

ALTER TABLE categories
    ADD COLUMN description  VARCHAR(255) NOT NULL DEFAULT '';

ALTER TABLE categories
    MODIFY description VARCHAR(255) NOT NULL;

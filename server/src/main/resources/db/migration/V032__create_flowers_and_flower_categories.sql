USE flowershop;

CREATE TABLE IF NOT EXISTS flowers (
                                       sku               VARCHAR(50)  PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    description       TEXT,
    short_description VARCHAR(255),
    price             DOUBLE       NOT NULL,
    image_url         VARCHAR(255),
    promotion_id      BIGINT NULL,
    CONSTRAINT fk_flowers_promotion
    FOREIGN KEY (promotion_id) REFERENCES promotions(id)
    ON UPDATE CASCADE ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- join table for @ManyToMany
CREATE TABLE IF NOT EXISTS flower_categories (
    flower_sku  VARCHAR(50) NOT NULL,
    category_id BIGINT      NOT NULL,
    PRIMARY KEY (flower_sku, category_id),
    CONSTRAINT fk_fc_flower
    FOREIGN KEY (flower_sku)  REFERENCES flowers(sku)
    ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_fc_category
    FOREIGN KEY (category_id) REFERENCES categories(id)
    ON UPDATE CASCADE ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

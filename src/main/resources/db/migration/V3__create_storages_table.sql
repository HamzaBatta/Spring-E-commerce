CREATE TABLE storages
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    name       VARCHAR(255)         NOT NULL,
    location   VARCHAR(255)         NOT NULL,
    product_id BIGINT               NOT NULL,
    quantity   INT DEFAULT 0        NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT storages_products_id_fk
        FOREIGN KEY (product_id) REFERENCES products (id)
            ON DELETE CASCADE
);

CREATE INDEX storages_products_id_fk ON storages (product_id);

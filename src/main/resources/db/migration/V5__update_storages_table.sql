ALTER TABLE storages
    DROP FOREIGN KEY storages_products_id_fk;

ALTER TABLE storages
    DROP INDEX storages_products_id_fk;

ALTER TABLE storages
    DROP COLUMN product_id,
    DROP COLUMN quantity;

CREATE TABLE storage_items
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    storage_id BIGINT                NOT NULL,
    product_id BIGINT                NOT NULL,
    quantity   INT DEFAULT 0         NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT storage_items_storages_id_fk
        FOREIGN KEY (storage_id) REFERENCES storages (id)
            ON DELETE CASCADE,
    CONSTRAINT storage_items_products_id_fk
        FOREIGN KEY (product_id) REFERENCES products (id)
            ON DELETE CASCADE,
    CONSTRAINT uk_storage_items_storage_product
        UNIQUE (storage_id, product_id)
);

CREATE INDEX storage_items_storage_id_fk ON storage_items (storage_id);
CREATE INDEX storage_items_products_id_fk ON storage_items (product_id);


ALTER TABLE orders
    ADD COLUMN storage_id BIGINT NULL;

ALTER TABLE orders
    ADD CONSTRAINT orders_storages_id_fk
        FOREIGN KEY (storage_id) REFERENCES storages (id);

CREATE INDEX orders_storages_id_fk ON orders (storage_id);


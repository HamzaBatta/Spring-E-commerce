CREATE TABLE invoices
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    order_id   BIGINT                NOT NULL UNIQUE,
    file_path  VARCHAR(255)          NULL,
    status     VARCHAR(20)           NULL,
    created_at DATETIME(6)           NULL,
    updated_at DATETIME(6)           NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT invoices_orders_fk
        FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE INDEX invoices_order_id_idx ON invoices (order_id);

CREATE TABLE orders
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    user_id    BIGINT               NOT NULL,
    status     VARCHAR(20)          NOT NULL DEFAULT 'CONFIRMED',
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT orders_users_id_fk
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX orders_users_id_fk ON orders (user_id);

CREATE TABLE order_items
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    order_id   BIGINT               NOT NULL,
    product_id BIGINT               NOT NULL,
    quantity   INT                  NOT NULL,
    unit_price DECIMAL(10, 2)       NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT order_items_orders_id_fk
        FOREIGN KEY (order_id) REFERENCES orders (id)
            ON DELETE CASCADE,
    CONSTRAINT order_items_products_id_fk
        FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE INDEX order_items_orders_id_fk ON order_items (order_id);
CREATE INDEX order_items_products_id_fk ON order_items (product_id);


CREATE TABLE payments
(
    id                        BIGINT AUTO_INCREMENT NOT NULL,
    order_id                  BIGINT                NOT NULL UNIQUE,
    stripe_payment_intent_id  VARCHAR(255)          NOT NULL UNIQUE,
    amount                    DECIMAL(10, 2)        NOT NULL,
    currency                  VARCHAR(10)           NOT NULL DEFAULT 'usd',
    status                    VARCHAR(20)           NOT NULL DEFAULT 'PENDING',
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT payments_orders_fk
        FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE INDEX payments_order_id_idx ON payments (order_id);

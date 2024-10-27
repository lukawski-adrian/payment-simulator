CREATE TABLE IF NOT EXISTS payment_accounts
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL
        CONSTRAINT payment_accounts_name_uq UNIQUE
);


CREATE TABLE IF NOT EXISTS payment_transaction_events
(
    id               SERIAL PRIMARY KEY,
    transaction_id   VARCHAR(64)    NOT NULL,
    transaction_type VARCHAR(64)    NOT NULL
        CONSTRAINT payment_transaction_events_transaction_type_in CHECK (transaction_type IN ('WITHDRAW', 'DEPOSIT', 'REPORT', 'BLOCK')),
    account_id       INTEGER        NOT NULL REFERENCES payment_accounts ON DELETE RESTRICT,
    amount           NUMERIC(15, 2) NOT NULL,
    created_on       TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX payment_transaction_events_transaction_id_transaction_type_uq ON payment_transaction_events (transaction_id, transaction_type);
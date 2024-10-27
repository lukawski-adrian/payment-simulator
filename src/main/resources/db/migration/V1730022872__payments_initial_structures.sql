CREATE TABLE IF NOT EXISTS payment_accounts
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL
        CONSTRAINT payment_accounts_name_uq UNIQUE
);

CREATE TABLE IF NOT EXISTS payment_transactions
(
    id             SERIAL PRIMARY KEY,
    transaction_id VARCHAR(64) NOT NULL
        CONSTRAINT payment_transactions_transaction_id_uq UNIQUE
);

CREATE TABLE IF NOT EXISTS payment_transaction_events
(
    id             SERIAL PRIMARY KEY,
    transaction_id INTEGER        NOT NULL REFERENCES payment_transactions ON DELETE RESTRICT,
    event_type     VARCHAR(64)    NOT NULL
        CONSTRAINT payment_transaction_events_event_type_in CHECK (event_type IN ('WITHDRAW', 'DEPOSIT', 'REPORT', 'BLOCK')),
    account_id     INTEGER        NOT NULL REFERENCES payment_accounts ON DELETE RESTRICT,
    amount         NUMERIC(15, 2) NOT NULL,
    created        TIMESTAMP      NOT NULL
);

CREATE UNIQUE INDEX payment_transaction_events_transaction_id_event_type_uq ON payment_transaction_events (transaction_id, event_type);
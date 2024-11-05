ALTER TABLE payment_transaction_events
    RENAME TO money_transfers;

CREATE TABLE IF NOT EXISTS payment_transactions
(
    id                   SERIAL PRIMARY KEY,
    transaction_id       VARCHAR(36)    NOT NULL,
    transaction_type     VARCHAR(32)    NOT NULL
        CONSTRAINT payment_transactions_transaction_type_in CHECK (transaction_type IN ('VALIDATED', 'STARTED', 'VERIFIED', 'SETTLED', 'BLOCKED')),
    sender_account_id    INTEGER        NOT NULL REFERENCES payment_accounts ON DELETE RESTRICT,
    recipient_account_id INTEGER        NOT NULL REFERENCES payment_accounts ON DELETE RESTRICT,
    amount               NUMERIC(15, 2) NOT NULL,
    comment              TEXT,
    created_on           TIMESTAMP      NOT NULL DEFAULT NOW()
);


INSERT INTO payment_transactions (transaction_id, transaction_type, sender_account_id, recipient_account_id, amount, created_on)
WITH fallback_account AS (SELECT id FROM payment_accounts a WHERE name = 'FALLBACK_ACCOUNT'),
     deposit_transaction AS (SELECT mtd.account_id, mtd.transaction_id
                             FROM money_transfers mtd
                             WHERE mtd.transaction_type = 'DEPOSIT')
SELECT mt.transaction_id,
       'VALIDATED'                                       AS transaction_type,
       mt.account_id                                     AS sender_account_id,
       COALESCE((SELECT dt.account_id FROM deposit_transaction dt WHERE dt.transaction_id = mt.transaction_id),
                (SELECT fa.id FROM fallback_account fa)) AS recipient_account_id,
       -mt.amount                                        AS amount,
       mt.created_on                                     AS created_on
FROM money_transfers mt
WHERE mt.transaction_type = 'WITHDRAW';



INSERT INTO payment_transactions (transaction_id, transaction_type, sender_account_id, recipient_account_id, amount, created_on)
WITH fallback_account AS (SELECT id FROM payment_accounts a WHERE name = 'FALLBACK_ACCOUNT'),
     deposit_transaction AS (SELECT mtd.account_id, mtd.transaction_id
                             FROM money_transfers mtd
                             WHERE mtd.transaction_type = 'DEPOSIT')
SELECT mt.transaction_id,
       'STARTED'                                         AS transaction_type,
       mt.account_id                                     AS sender_account_id,
       COALESCE((SELECT dt.account_id FROM deposit_transaction dt WHERE dt.transaction_id = mt.transaction_id),
                (SELECT fa.id FROM fallback_account fa)) AS recipient_account_id,
       -mt.amount                                        AS amount,
       mt.created_on                                     AS created_on
FROM money_transfers mt
WHERE mt.transaction_type = 'WITHDRAW';



INSERT INTO payment_transactions (transaction_id, transaction_type, sender_account_id, recipient_account_id, amount, created_on)
WITH fallback_account AS (SELECT id FROM payment_accounts a WHERE name = 'FALLBACK_ACCOUNT'),
     withdraw_transaction AS (SELECT mtw.account_id, mtw.transaction_id
                              FROM money_transfers mtw
                              WHERE mtw.transaction_type = 'WITHDRAW')
SELECT mt.transaction_id,
       'VERIFIED'                                        AS trasaction_type,
       COALESCE((SELECT wt.account_id FROM withdraw_transaction wt WHERE wt.transaction_id = mt.transaction_id),
                (SELECT fa.id FROM fallback_account fa)) AS sender_account_id,
       mt.account_id                                     AS recipient_account_id,
       mt.amount                                         AS amount,
       mt.created_on                                     AS created_on
FROM money_transfers mt
WHERE mt.transaction_type = 'DEPOSIT';


INSERT INTO payment_transactions (transaction_id, transaction_type, sender_account_id, recipient_account_id, amount, created_on)
WITH fallback_account AS (SELECT id FROM payment_accounts a WHERE name = 'FALLBACK_ACCOUNT'),
     withdraw_transaction AS (SELECT mtw.account_id, mtw.transaction_id
                              FROM money_transfers mtw
                              WHERE mtw.transaction_type = 'WITHDRAW')
SELECT mt.transaction_id,
       'SETTLED'                                         AS trasaction_type,
       COALESCE((SELECT wt.account_id FROM withdraw_transaction wt WHERE wt.transaction_id = mt.transaction_id),
                (SELECT fa.id FROM fallback_account fa)) AS sender_account_id,
       mt.account_id                                     AS recipient_account_id,
       mt.amount                                         AS amount,
       mt.created_on                                     AS created_on
FROM money_transfers mt
WHERE mt.transaction_type = 'DEPOSIT';



INSERT INTO payment_transactions (transaction_id, transaction_type, sender_account_id, recipient_account_id, amount, created_on,
                                  comment)
WITH fallback_account AS (SELECT id FROM payment_accounts a WHERE name = 'FALLBACK_ACCOUNT')
SELECT mt.transaction_id,
       'BLOCKED'                                         as trasaction_type,
       mt.account_id                                     as sender_account_id,
       COALESCE((SELECT dt.account_id
                 FROM money_transfers dt
                 WHERE dt.transaction_type = 'DEPOSIT'
                   AND dt.transaction_id = mt.transaction_id),
                (SELECT fa.id FROM fallback_account fa)) as recipient_account_id,
       -(SELECT wt.amount
         FROM money_transfers wt
         WHERE wt.transaction_type = 'WITHDRAW'
           AND wt.transaction_id = mt.transaction_id)    as amount,
       mt.created_on                                     as created_on,
       mt.comment
FROM money_transfers mt
WHERE mt.transaction_type = 'BLOCK';


-- REPORT should be redirected to dead letter queue
DELETE
FROM money_transfers mt
WHERE mt.transaction_type IN ('REPORT', 'BLOCK');



ALTER TABLE money_transfers
    RENAME COLUMN transaction_type TO transfer_type;

ALTER TABLE money_transfers
    DROP COLUMN comment;


DROP INDEX IF EXISTS payment_transaction_events_transaction_id_transaction_type_uq;

ALTER TABLE money_transfers
    DROP CONSTRAINT IF EXISTS payment_transaction_events_transaction_type_amount_consistency;
ALTER TABLE money_transfers
    ADD CONSTRAINT money_transfers_transfer_type_amount_consistency
        CHECK ((transfer_type = 'WITHDRAW' AND amount < 0) OR (transfer_type = 'DEPOSIT' AND amount > 0));


ALTER TABLE money_transfers
    DROP CONSTRAINT IF EXISTS payment_transaction_events_transaction_type_in;
ALTER TABLE money_transfers
    ADD CONSTRAINT money_transfers_transfer_type_in
        CHECK (transfer_type IN ('WITHDRAW', 'DEPOSIT'));

DROP INDEX IF EXISTS payment_transaction_events_transaction_id_transaction_type_uq;
CREATE UNIQUE INDEX money_transfers_transaction_id_transfer_type_uq ON money_transfers (transaction_id, transfer_type);


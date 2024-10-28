ALTER TABLE payment_transaction_events ADD CONSTRAINT payment_transaction_events_transaction_type_amount_consistency
    CHECK ((transaction_type = 'WITHDRAW' AND amount < 0) OR (transaction_type = 'DEPOSIT' AND amount > 0)
               OR (transaction_type IN ('REPORT', 'BLOCK') AND amount = 0));
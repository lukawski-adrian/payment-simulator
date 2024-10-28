INSERT INTO payment_accounts (name) values ('ACC1');
INSERT INTO payment_accounts (name) values ('ACC2');
INSERT INTO payment_accounts (name) values ('ACC3');


INSERT INTO payment_transaction_events (transaction_id, transaction_type, amount, account_id)
        values ('287cb1c9-9177-40b4-9be2-f129d31db487', 'DEPOSIT', 1000, (SELECT id FROM payment_accounts WHERE payment_accounts.name = 'ACC1'));

INSERT INTO payment_transaction_events (transaction_id, transaction_type, amount, account_id)
        values ('81a76159-fd5f-45c1-8f12-fc82a0d9e8e7', 'DEPOSIT', 1100, (SELECT id FROM payment_accounts WHERE payment_accounts.name = 'ACC2'));

INSERT INTO payment_transaction_events (transaction_id, transaction_type, amount, account_id)
        values ('4958e659-6141-475e-b7c4-cc4f9b053bd4', 'DEPOSIT', 1200, (SELECT id FROM payment_accounts WHERE payment_accounts.name = 'ACC3'));
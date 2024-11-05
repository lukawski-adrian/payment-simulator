# Payment simulator

Simple payment simulator application. Simulates money transfer between two accounts with some additional checks in the meantime.

## Architecture and features

### Payment flow overview

Main `payment flow` designed according to `chain of responsibility` design pattern. `TransactionRequest` is passed into
chain of independent `TransactionHandler`s.

Assumptions:

- every transaction is traceable, replayable and evidentiary via set of `PaymentTransactionEvent`
- every `TransactionHandler` is responsible for specific isolated part of the `payment flow`
- every `TransactionHandler` decides if a given request will be passed to the next handler or proceeding will be finished at the
  moment
- every `TransactionHandler` executes idempotent operations (safe in case of re-execution)
- in case of system failure at any step `payment flow` will be retried 3 times for a given request
- in case of failed disaster recovery after retries as a fallback transaction will be logged (TODO: send to DLQ)
- `GuardTransactionHandler` verifies in `paralell` arbitrary number of checks (at the moment as an example there are two mocked
  guards: `FraudDetectionGuard` and `ComplianceGuard`)
- in case when any of transaction checks fail transaction will be blocked (transaction event with type `BLOCKED` will be stored)
- at the moment application considered as internal service (minimal input data validation)

### Simplified payment flow overview

![payment_flow](docs/payment_flow.png)

### Main aspects:

- money transfer stored as event log:
    - every transaction is replayable and evidentiary
    - in case of transaction check failure transaction is blocked
    - in case of system failure transaction is reported
- designed for failure:
    - bulkhead pattern on the input side
    - retryable `payment flow` with idempotent operations
    - partially prepared for integration with event-driven or messaging architecture
- `payment flow` design:
    - prepared for extension -> `chain of responsibility` design pattern
    - next transaction steps clearly isolated by `pl.varlab.payment.transaction.handler.TransactionHandler` interface
    - utilizes a few shorter (smaller) transactions instead of single long-running database transactions
- scalable, lightweight, modern:
    - synchronization point between instances has been designed on the database transaction level
    - `concurrent` transactions processing
    - `async` and `paralell` transaction verification checks (separate dedicated thread pool for `guard` checks)
    - utilizes lightweight `java21 virtual threads`
    - uses modern java lang features like: `Records`, `String Templates`, `Text Blocks`, `Sealed Classes` etc.
- prepared for observability (`spring actuator`)
    - basic health status exposed on the `/api/actuator/health` endpoint
    - actual `@Retry` stats available on the `/api/actuator/retries` and `/api/actuator/retryevents` endpoints
    - actual `@Bulkhead` stats available on the `/api/actuator/bulkheads` and `/api/actuator/bulkheadevents` endpoints
- documented
    - `readme.md` file
    - `swagger-ui` and `OpenAPI` exposed on the `/api/swagger-ui/index.html`

### Prerequisites

If running with embedded in memory `H2` database, `Java21` should be the only one requirement.

You should be able to run the app via `maven wrapper`:

```bash
./mvnw spring-boot:run
```

### Running on `PostgreSQL`

Originally designed for `PostgreSQL`. To run with `PostgreSQL` you need to provide working `PostgreSQL` instance
and set `env` variables:

| Environment variable | Description                                    | Example                                   |
|----------------------|------------------------------------------------|-------------------------------------------
| DB_DRIVER_CLASS      | JDBC `PostgreSQL` driver class name            | org.postgresql.Driver                     |
| DB_URL               | JDBC `PostgreSQL` url to the payments database | jdbc:postgresql://localhost:5432/payments |
| DB_USERNAME          | Payments database username                     | databaseUsername                          |
| DB_PASSWORD          | Payments database username password            | secret                                    |

### Give it a try

When the app is running goto `swagger-ui`: http://localhost:8080/api/swagger-ui/index.html and navigate between available endpoints. 

Initial SQL script creates three example accounts with deposit on them.

You can get all the accounts balance via `/api/v1/accounts` endpoint.

Example request:
```
GET /api/v1/accounts HTTP/1.1
Host: localhost:8080
```

Example response:
```json
[
  {
    "accountNumber": "ACC3",
    "balance": 1280
  },
  {
    "accountNumber": "ACC1",
    "balance": 830
  },
  {
    "accountNumber": "ACC2",
    "balance": 970
  }
]
```

After accounts balance verification give a try to `transaction-controller`:

Example request for new transaction:
```
POST /api/v1/transactions HTTP/1.1
Host: localhost:8080

{
  "senderAccountNumber": "ACC3",
  "recipientAccountNumber": "ACC2",
  "amount": 100
}
```

You will get `201 CREATED` and `transactionId` in the response body if everything went well.
```
{
  "transactionId": "92a70c19-b1a4-4f6e-9561-d47b76645d58"
}
```

You can get also `400 Bad Request, 404 Not Found, 409 Conflict` with an error in the response body when something went wrong.

Example:
```
{
  "status": "CONFLICT",
  "message": "Fraud detected (divisor 11)"
}
```


You can rerun the same transaction many times via (`PUT /api/v1/transactions`) endpoint but only with the same data.
```
PUT /api/v1/transactions HTTP/1.1
Host: localhost:8080

{
  "transactionId": "d0fe75f6-7cae-402b-a106-93a58d261c47",
  "senderAccountNumber": "ACC3",
  "recipientAccountNumber": "ACC2",
  "amount": 100
}
```

If you change e.g. amount it'll be treated as `FraudException` and transaction will be blocked.


### TODOs:

- `@CircuitBreaker` on the guards
- authentication
- authorization
- docker
- more unit tests
- more integration tests
- compliance endpoint
- retry `async` mechanism (dlq)
- event-driven platform integration
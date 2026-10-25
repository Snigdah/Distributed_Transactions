# Seata POC - Step 1: 4 independent services (no Seata yet)

Story: customer **C-001** has **5000 Tk**. Product **P-100** has **10** in stock.
The customer buys 1 item for 1000 Tk.

| Service | Port | Job |
|---|---|---|
| discovery-service | 8761 | Eureka. Every service registers here |
| order-service | 8081 | Takes the order, calls inventory + payment with Feign |
| inventory-service | 8082 | Deducts stock |
| payment-service | 8083 | Charges the wallet |

Each service is its own Maven project with its own database (PostgreSQL now, Oracle works too).

## Database
1. Create the databases once: `db/postgres-setup.sql` (or `db/oracle-setup.sql` for Oracle).
2. In each service's `application.yml`, set `username` and `password`.
3. Tables are created automatically on start, and starting data is loaded from `data.sql`.

To switch to Oracle: change only the 3 `datasource` lines in `application.yml` (example is in the file). No code change.

## Code layout (same in every service)
```
controller/  -> REST endpoints
service/     -> business logic
repository/  -> database access
entity/      -> database tables
dto/         -> request bodies
client/      -> Feign clients (order-service only)
```

## Run
Requires JDK 17 or 21, and Lombok plugin enabled in IntelliJ (Settings > Build > Compiler > Annotation Processors > Enable). Start in this order, in each service folder:
```
mvn spring-boot:run
```
1. discovery-service
2. inventory-service
3. payment-service
4. order-service

Open http://localhost:8761 to see all 3 services registered. Wait ~30 seconds before the first order.

## Test

**1. Successful order** -> status CONFIRMED, wallet 4000, stock 9
```
curl -X POST http://localhost:8081/api/orders -H "Content-Type: application/json" -H "Idempotency-Key: key-1" -d "{\"customerId\":\"C-001\",\"productId\":\"P-100\",\"quantity\":1,\"amount\":1000}"
curl http://localhost:8083/api/payments/wallet/C-001
curl http://localhost:8082/api/inventory/P-100
```

**2. Same request again (same key)** -> same order returned, no second charge
```
curl -X POST http://localhost:8081/api/orders -H "Content-Type: application/json" -H "Idempotency-Key: key-1" -d "{\"customerId\":\"C-001\",\"productId\":\"P-100\",\"quantity\":1,\"amount\":1000}"
```

**3. Payment fails** -> status CANCELLED
```
curl -X POST "http://localhost:8083/api/payments/fail-mode?enabled=true"
curl -X POST http://localhost:8081/api/orders -H "Content-Type: application/json" -H "Idempotency-Key: key-2" -d "{\"customerId\":\"C-001\",\"productId\":\"P-100\",\"quantity\":1,\"amount\":1000}"
curl http://localhost:8082/api/inventory/P-100
curl -X POST "http://localhost:8083/api/payments/fail-mode?enabled=false"
```
Expected problem (no Seata yet): order is CANCELLED but stock went 9 -> 8 and was **not** given back.
Seata will fix this in the next step.

## Look inside the databases
Use DBeaver / pgAdmin / SQL Developer on `order_db`, `inventory_db`, `payment_db`.
Tables: `orders`, `product_stock`, `stock_ledger`, `wallet`, `payment_ledger`.

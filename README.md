# Seata POC - XA mode (Apache Seata 2.6.0)

> Branch `feature/XA`. The AT version of this POC is on branch `feature/AT`.
> Only difference in code: `seata.data-source-proxy-mode: XA` in the 3 services' `application.yml`.

Customer **C-001** has **5000 Tk**.

| Product | Price | Stock |
|---|---|---|
| P-100 | 1000 | 10 |
| P-200 | 10000 | 10 |

| Service | Port | Seata role |
|---|---|---|
| discovery-service | 8761 | - |
| order-service | 8081 | TM: starts the global transaction |
| inventory-service | 8082 | RM |
| payment-service | 8083 | RM |
| seata-server | 192.168.10.56:18091 | TC: coordinator |

Stack: Spring Boot 3.5.6, Spring Cloud 2025.0.0, Java 21, Apache Seata 2.6.0 (client + server), Lombok, PostgreSQL (Oracle ready).

## Flow (one global transaction)
`POST /api/orders` → reduce stock (inventory_db) → take money (payment_db) → save order CONFIRMED (order_db)

## How XA mode works
1. **Phase 1, prepare:** each service runs its SQL inside an XA branch, then the database runs `XA PREPARE`.
   The change is saved but **not committed**, and the row stays **locked by the database**.
2. **Phase 2, decide:** the Seata server tells every branch the result.
   - All prepared OK: `XA COMMIT` everywhere, and the new values become visible.
   - Any failure: `XA ROLLBACK` everywhere, and the prepared changes are thrown away.

| | AT (`feature/AT`) | XA (this branch) |
|---|---|---|
| Change in phase 1 | committed at once, old value saved in `undo_log` | prepared, **not committed** |
| Others read P-200 during the transaction | see **9** (in-between value) | see **10** (last committed value) |
| Rollback done by | Seata, from `undo_log` | the database (`XA ROLLBACK`) |
| `undo_log` table | needed | **not needed** |
| Row locks | short | held until phase 2 (slower under load) |

The global transaction id (XID) travels with every Feign call in the HTTP header `TX_XID`:
`FeignXidConfig` (order-service) adds it, `SeataXidConfig` (inventory, payment) reads it.

## Seata server

Runs on **192.168.10.56** with Docker, in **DB store mode** (recommended for banking: survives restarts, supports more than one server, can be audited with SQL).

| Item | Value |
|---|---|
| Image | `apache/seata-server:2.6.0.jdk25` (official Apache image, pinned version) |
| Port | host `18091` → container `8091` (services connect to `192.168.10.56:18091`) |
| Store mode | `db` (session + lock) |
| Seata's own database | `seata_db` (separate from business databases) |
| Compose file | `seata-server/docker-compose.yml` (PostgreSQL), `seata-server/docker-compose.oracle.yml` (Oracle) |
| Web console | Not included in 2.6.0 (moved to the separate namingserver). Not needed for this POC |

The compose `environment` values (`SEATA_STORE_DB_...`) are the official `seata.store.*` settings
from the server's `application.example.yml`, written as environment variables.

Start / check on the server:
```
cd ~/docker-file/seata
docker compose up -d
docker logs -f seata-server-poc
```
Healthy log shows: `use lock store mode: db`, `use session store mode: db`, `Server started, service listen port: 8091`.

### Two kinds of tables (do not mix them)

| Table | Where | Purpose |
|---|---|---|
| `undo_log` | business DBs, **AT mode only** (scripts are on `feature/AT`); XA does not use it | lets Seata undo local changes in AT mode |
| `global_table` | `seata_db` | global transaction status |
| `branch_table` | `seata_db` | each service's part of a global transaction |
| `lock_table` | `seata_db` | global row locks |
| `distributed_lock` | `seata_db` | lets several Seata servers share background jobs |
| `vgroup_table` | `seata_db` | used by namingserver (harmless here) |

### Common Seata server errors

| Log message | Fix |
|---|---|
| `no pg_hba.conf entry for host "192.168.10.56"` | Add to `pg_hba.conf`: `host seata_db postgres 192.168.10.56/32 scram-sha-256`, then restart PostgreSQL |
| `relation "distributed_lock" does not exist` | Run `db/seata-server-postgresql.sql` in `seata_db`, then `docker restart seata-server-poc` |
| `Bind for 0.0.0.0:xxxx failed: port is already allocated` | Port used by another app on the server. Pick another host port and update `application.yml` |

## Database setup
All database steps (create databases, Seata tables, **turn on XA**), with where to run each command
(pgAdmin, PowerShell, server terminal, Oracle): see **[db/README.md](db/README.md)**.

## Setup (once)
1. Follow [db/README.md](db/README.md) (databases, Seata tables, turn on XA)
2. Set `username` / `password` in each service's `application.yml`
3. Start the Seata server (see above)
4. In IntelliJ: JDK 21, enable annotation processing (Lombok), reload Maven
5. Start: discovery → inventory → payment → order. Check http://localhost:8761

## Test (Windows Command Prompt)

Check values any time:
```
curl http://localhost:8082/api/inventory/P-100
curl http://localhost:8082/api/inventory/P-200
curl http://localhost:8083/api/payments/wallet/C-001
curl http://localhost:8081/api/orders
```

### Case 1 - Cheap product: success
```
curl -X POST http://localhost:8081/api/orders -H "Content-Type: application/json" -d "{\"customerId\":\"C-001\",\"productId\":\"P-100\",\"quantity\":1}"
```
Result: order `CONFIRMED`, amount 1000. P-100 stock **9**, wallet **4000**.

### Case 2 - Expensive product: payment fails, Seata rolls back
```
curl -X POST http://localhost:8081/api/orders -H "Content-Type: application/json" -d "{\"customerId\":\"C-001\",\"productId\":\"P-200\",\"quantity\":1}"
```
Result: `{"status":"ROLLED_BACK","reason":"Not enough balance to pay 10000.00"}`
- inventory log shows stock of P-200 is **9**, but only inside its own prepared XA branch (not committed)
- payment fails, Seata sends `XA ROLLBACK`: the prepared change is thrown away
- P-200 stock is **10**, wallet still **4000**, no new order in the list

### See the difference from AT (optional)
Put a breakpoint in **payment-service** `PaymentService.charge()` (inventory has already prepared),
send the P-200 request, and while it is paused run in pgAdmin:
```sql
-- inventory_db
SELECT quantity FROM product_stock WHERE product_id = 'P-200';  -- 10 (AT would show 9)
SELECT gid, prepared FROM pg_prepared_xacts;                     -- 1 row: inventory's prepared branch
```
Resume: the prepared row disappears and the stock is still 10.
Don't pause longer than 60 seconds (the default global transaction timeout).

That is XA mode: nothing is committed until every service is ready, so nobody sees a half-done value.

## Adding a new business microservice to XA mode
1. Add dependency `org.apache.seata:seata-spring-boot-starter:2.6.0`
2. Copy the `seata:` block into its `application.yml` (same `tx-service-group`, change `application-id`)
3. Make sure its database has XA turned on (PostgreSQL `max_prepared_transactions` > 0, Oracle grants). No `undo_log`
4. Copy `SeataXidConfig` (to join) and `FeignXidConfig` (if it calls other services)
5. Use normal `@Transactional`; throw an exception on failure so Seata rolls back

Rules: only database changes can be undone (not emails or external APIs); keep XA transactions short, because
rows stay locked until phase 2; do not mix AT and XA services in one global transaction.

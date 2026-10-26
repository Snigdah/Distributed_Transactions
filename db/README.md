# Database setup - XA mode (branch `feature/XA`)

Everything you run on the databases, in order: **what** to run, **where** to run it, and **why**.

---

## Why XA needs a database setting

In XA mode a global transaction has two phases:

1. **Prepare** - each database does its change (for example stock 10 -> 9) but does **not** commit.
   It saves the change and says "I am ready, waiting for the final word".
   This waiting state is called a **prepared transaction**.
2. **Decide** - the Seata server tells every database: commit all, or roll back all.

So the database must be able to **keep prepared transactions** and finish them later, even after a crash.

| | PostgreSQL | Oracle |
|---|---|---|
| Can keep prepared transactions by default? | **No.** `max_prepared_transactions = 0` | **Yes** |
| What we must do | Give it space for them: set `max_prepared_transactions = 100`, then restart | Give our users permission to see and finish them after a crash (grants) |
| What happens if we skip it | Every XA order fails: *"prepared transactions are disabled"* | Normal orders work, but after a crash a prepared transaction can stay stuck and keep rows locked |

Simple picture: prepared transactions wait in a **parking lot** until the final decision.
PostgreSQL's parking lot has **0 spaces** by default, so we give it **100**.
Oracle already has the parking lot; we only give our users the **key** to clean it up after a crash.

---

## Where to run commands

| Tool | What it is | How to open it |
|---|---|---|
| **pgAdmin** (PostgreSQL) | screen tool to run SQL | Start menu -> **pgAdmin 4** |
| **SQL Shell (psql)** (PostgreSQL) | console to run SQL | Start menu -> **SQL Shell (psql)** |
| **PowerShell (Admin)** | restart PostgreSQL on Windows | Start menu -> type `PowerShell` -> right-click -> **Run as administrator** |
| **Server terminal** | Linux server with the Seata server | `ssh leads@192.168.10.56`, then `cd ~/docker-file/seata` |
| **SQL Developer** (Oracle) | screen tool to run SQL | open SQL Developer -> connection as **SYS** |
| **SQL\*Plus** (Oracle) | console to run SQL | Command Prompt -> `sqlplus` (see below) |

### How to run SQL in pgAdmin (click by click)
1. Open **pgAdmin 4**.
2. Left panel: expand **Servers** -> **PostgreSQL 18** (enter the `postgres` password if asked).
3. Expand **Databases** and click the database the step says (for example `postgres` or `seata_db`).
4. Top menu **Tools** -> **Query Tool** (or right-click the database -> **Query Tool**).
5. Paste the SQL, or click the **folder icon** to open a `.sql` file from this folder.
6. Press **F5** (or the play button). The bottom panel shows the result.

### How to run SQL in SQL Shell (psql)
1. Start menu -> **SQL Shell (psql)**.
2. Press **Enter** for Server, Database, Port, Username (defaults: `localhost`, `postgres`, `5432`, `postgres`), then type the password.
3. At `postgres=#` type the SQL and press Enter. Every command ends with `;`.
4. To switch database: `\c seata_db`. To run a file: `\i 'D:/Distributed_Transactions/db/seata-server-postgresql.sql'`. To quit: `\q`.

### How to run SQL in Oracle SQL Developer (click by click)
1. Open **SQL Developer**.
2. **Connections** panel -> green **+** (New Connection).
3. Name: `sys-pdb`, Username: `sys`, Password: your SYS password, **Role: SYSDBA**,
   Hostname: your Oracle host, Port: `1521`, **Service name**: your PDB (for example `FREEPDB1`).
4. **Test** -> **Connect**. A worksheet opens.
5. Paste the SQL (or **File** -> **Open** a `.sql` file) and press **F5** (Run Script).

### How to run SQL in SQL\*Plus (console)
In Command Prompt:
```
sqlplus sys@//localhost:1521/FREEPDB1 as sysdba
```
Type the SYS password. Run a file with `@D:\Distributed_Transactions\db\oracle-xa-grants.sql`. Quit with `exit`.
To connect as another user: `sqlplus seata_db@//localhost:1521/FREEPDB1`.

---

## PostgreSQL - step by step

### Step 1 - Create the databases (once)
- **Why:** one database per service, plus one for the Seata server.
- **Where:** pgAdmin Query Tool on database **`postgres`** (or psql)
- **File:** `postgres-setup.sql`
```sql
CREATE DATABASE order_db;
CREATE DATABASE inventory_db;
CREATE DATABASE payment_db;
CREATE DATABASE seata_db;
```
Then in pgAdmin right-click **Databases** -> **Refresh**: you see the 4 databases.

### Step 2 - Create the Seata server tables (once)
- **Why:** the Seata server (DB mode) keeps every global transaction, branch and lock in these tables.
- **Where:** pgAdmin Query Tool on database **`seata_db`** only (psql: `\c seata_db` first)
- **File:** `seata-server-postgresql.sql` (official Apache Seata v2.6.0 script)
- Creates `global_table`, `branch_table`, `lock_table`, `distributed_lock`, `vgroup_table`.

### Step 3 - Turn on XA (prepared transactions) (once)
- **Why:** see "Why XA needs a database setting" above. Without it every XA order fails.

**3a. Save the setting**
- **Where:** pgAdmin Query Tool on database **`postgres`**, as user `postgres` (or psql)
- **File:** `postgres-xa-setup.sql`
```sql
ALTER SYSTEM SET max_prepared_transactions = 100;
```
Result: `ALTER SYSTEM`. This saves the setting (same as editing `postgresql.conf`), but it is **not active yet**.

**3b. Restart PostgreSQL** (PostgreSQL reserves the space only when it starts)
- **Where:** PowerShell (Admin) on your PC
```powershell
Restart-Service postgresql-x64-18
Get-Service postgresql-x64-18        # Status must be: Running
```
Not sure of the name? `Get-Service *postgres*`.

**3c. Check it is on**
- **Where:** pgAdmin Query Tool (click to reconnect if it says the connection was lost) or psql
```sql
SHOW max_prepared_transactions;      -- must show 100 (not 0)
```

**3d. Refresh Seata's connections** (the restart closed Seata's connections to `seata_db`)
- **Where:** server terminal
```bash
docker restart seata-server-poc
docker logs --since 1m seata-server-poc     # ends with "Server started", no errors after it
```

### Watch XA working (optional)
- **Where:** pgAdmin Query Tool on a business database, for example **`inventory_db`**
```sql
SELECT gid, prepared, database FROM pg_prepared_xacts;
```
Normally **empty**. A row appears only while a branch is prepared and waiting for the final decision.
Tip: breakpoint in payment-service `PaymentService.charge()`, send the P-200 order, run the query on
`inventory_db` -> inventory's prepared branch is there. Resume -> the row is gone, stock still 10.
Do not pause longer than 60 seconds (default global transaction timeout).

---

## Oracle - step by step (later)

### Step 1 - Create the schemas (once)
- **Why:** in Oracle one user = one schema; one per service, plus one for the Seata server.
- **Where:** SQL Developer / SQL\*Plus as **SYS**, in the PDB
- **File:** `oracle-setup.sql` (creates `order_db`, `inventory_db`, `payment_db`, `seata_db`)

### Step 2 - Create the Seata server tables (once)
- **Why:** same as PostgreSQL step 2.
- **Where:** connected as user **`seata_db`** (not SYS)
- **File:** `seata-server-oracle.sql` (official Apache Seata v2.6.0 script)

### Step 3 - XA grants (once)
- **Why:** Oracle already supports prepared (XA) transactions, so **no setting and no restart**.
  But if a service crashes between prepare and commit, the prepared transaction stays "in doubt" and keeps
  rows locked. These grants let our users **see** in-doubt transactions and **finish** them (commit or roll back),
  so Seata can recover. Without them, recovery fails with "insufficient privileges".
- **Where:** SQL Developer / SQL\*Plus as **SYS**, in the PDB
- **File:** `oracle-xa-grants.sql`
```sql
GRANT SELECT ON sys.dba_pending_transactions TO order_db, inventory_db, payment_db;  -- see in-doubt XA transactions
GRANT SELECT ON sys.pending_trans$          TO order_db, inventory_db, payment_db;  -- internal list of them
GRANT SELECT ON sys.dba_2pc_pending         TO order_db, inventory_db, payment_db;  -- their state
GRANT EXECUTE ON sys.dbms_system            TO order_db, inventory_db, payment_db;  -- system helper used in recovery
GRANT FORCE ANY TRANSACTION                 TO order_db, inventory_db, payment_db;  -- allowed to commit/roll back them
```
Result: `Grant succeeded.` for each line.

### Watch XA working (optional)
- **Where:** as SYS
```sql
SELECT * FROM dba_2pc_pending;
```
Normally empty. Rows appear only for in-doubt (stuck) XA transactions.

### Point everything to Oracle
- Services: in each `application.yml`, switch the 3 `datasource` lines to the Oracle example in the file.
- Seata server: use `seata-server/docker-compose.oracle.yml`.

---

## Files in this folder

| File | Run where | When |
|---|---|---|
| `postgres-setup.sql` | PostgreSQL, database `postgres` | once |
| `seata-server-postgresql.sql` | PostgreSQL, database `seata_db` | once |
| `postgres-xa-setup.sql` | PostgreSQL, database `postgres`, then restart PostgreSQL | once |
| `oracle-setup.sql` | Oracle, as SYS | later |
| `seata-server-oracle.sql` | Oracle, as `seata_db` | later |
| `oracle-xa-grants.sql` | Oracle, as SYS | later |

Business tables (`orders`, `product_stock`, `wallet`) are created by each service on start, and starting data
comes from each service's `data.sql`. **No `undo_log` is needed in XA mode.**

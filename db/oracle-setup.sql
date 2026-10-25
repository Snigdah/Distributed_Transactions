-- Run once as SYSTEM, connected to the PDB (e.g. FREEPDB1).
-- In Oracle, one user = one schema. One schema per service (+ one for Seata later).
CREATE USER order_db     IDENTIFIED BY your_password QUOTA UNLIMITED ON USERS;
CREATE USER inventory_db IDENTIFIED BY your_password QUOTA UNLIMITED ON USERS;
CREATE USER payment_db   IDENTIFIED BY your_password QUOTA UNLIMITED ON USERS;
CREATE USER seata_db     IDENTIFIED BY your_password QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO order_db, inventory_db, payment_db, seata_db;

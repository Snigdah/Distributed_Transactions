-- Run once as the postgres admin user.
-- One database per service (+ one for Seata later).
CREATE DATABASE order_db;
CREATE DATABASE inventory_db;
CREATE DATABASE payment_db;
CREATE DATABASE seata_db;

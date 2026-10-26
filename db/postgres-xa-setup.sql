-- XA mode needs PostgreSQL "prepared transactions" (XA PREPARE). They are OFF by default (0).
-- Run ONCE as the postgres admin user (any database), then RESTART PostgreSQL.
-- (Same as editing max_prepared_transactions in postgresql.conf.)

ALTER SYSTEM SET max_prepared_transactions = 100;

-- After restarting PostgreSQL, check:
--   SHOW max_prepared_transactions;   -- must be 100, not 0
--
-- See prepared (waiting) XA branches at any time:
--   SELECT gid, prepared, owner, database FROM pg_prepared_xacts;

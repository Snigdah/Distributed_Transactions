-- XA mode on Oracle: let each business schema recover prepared (in-doubt) XA transactions.
-- Run ONCE as SYS, connected to the PDB (e.g. FREEPDB1). Oracle supports XA out of the box;
-- no parameter change is needed, only these grants.

GRANT SELECT ON sys.dba_pending_transactions TO order_db, inventory_db, payment_db;
GRANT SELECT ON sys.pending_trans$          TO order_db, inventory_db, payment_db;
GRANT SELECT ON sys.dba_2pc_pending         TO order_db, inventory_db, payment_db;
GRANT EXECUTE ON sys.dbms_system            TO order_db, inventory_db, payment_db;
GRANT FORCE ANY TRANSACTION                 TO order_db, inventory_db, payment_db;

-- See in-doubt (prepared) XA transactions at any time (as SYS):
--   SELECT * FROM dba_2pc_pending;

-- Seata AT mode undo_log - Oracle
-- Run in EVERY business schema that joins a global transaction (order_db, inventory_db, payment_db).
-- The Seata server does NOT need it.
-- Official source (Apache Seata v2.6.0):
-- https://raw.githubusercontent.com/apache/incubator-seata/v2.6.0/script/client/at/db/oracle.sql
CREATE TABLE undo_log
(
    id            NUMBER(19)    NOT NULL,
    branch_id     NUMBER(19)    NOT NULL,
    xid           VARCHAR2(128) NOT NULL,
    context       VARCHAR2(128) NOT NULL,
    rollback_info BLOB          NOT NULL,
    log_status    NUMBER(10)    NOT NULL,
    log_created   TIMESTAMP(0)  NOT NULL,
    log_modified  TIMESTAMP(0)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ux_undo_log UNIQUE (xid, branch_id)
);
CREATE INDEX ix_log_created ON undo_log(log_created);
COMMENT ON TABLE undo_log IS 'AT transaction mode undo table';
COMMENT ON COLUMN undo_log.branch_id is 'branch transaction id';
COMMENT ON COLUMN undo_log.xid is 'global transaction id';
COMMENT ON COLUMN undo_log.context is 'undo_log context,such as serialization';
COMMENT ON COLUMN undo_log.rollback_info is 'rollback info';
COMMENT ON COLUMN undo_log.log_status is '0:normal status,1:defense status';
COMMENT ON COLUMN undo_log.log_created is 'create datetime';
COMMENT ON COLUMN undo_log.log_modified is 'modify datetime';

-- Generate ID using sequence and trigger
CREATE SEQUENCE UNDO_LOG_SEQ START WITH 1 INCREMENT BY 1;

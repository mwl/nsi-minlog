CREATE INDEX log_cpr_index ON LogEntry (`cprNrBorger`) USING BTREE;
--CREATE INDEX log_cpr_and_timestamp_hash_index ON LogEntry (`cprNrBorger`, `tidspunkt`) USING BTREE;

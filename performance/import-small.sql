DROP DATABASE IF EXISTS minlogps;
CREATE DATABASE minlogps;
USE minlogps;

source ../minlogudtraekservice/src/main/resources/db/migration/V1__Initial_schema.sql

SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS = 0;
SET sql_log_bin = 0; 

load data infile '/Users/kpi/Documents/work/java/nsi-minlog/performance/data/logentry-small.csv' into table logentry fields terminated by ',' enclosed by '"' lines terminated by '\n' (regKode, cprNrBorger, bruger, ansvarlig, orgUsingId, systemName, handling, sessionId, tidspunkt);

SET sql_log_bin = 1;
SET UNIQUE_CHECKS = 1;
SET FOREIGN_KEY_CHECKS = 1;

-- create a table so we can easy filter our dataset
-- We figure out how many logentries a given CPR has.
CREATE TABLE occurrences(cprNrBorger varchar(10), occurrence int);
INSERT INTO occurrences(cprNrBorger, occurrence) SELECT cprNrBorger, COUNT(*) AS c FROM logentry GROUP BY cprNrBorger;


\! rm '/users/kpi/Documents/work/java/nsi-minlog/performance/data/usedCpr-small_0-30.csv'
\! rm '/users/kpi/Documents/work/java/nsi-minlog/performance/data/usedCpr-small_200-300.csv'
\! rm '/users/kpi/Documents/work/java/nsi-minlog/performance/data/usedCpr-small_2000-3000.csv'


SELECT cprNrBorger FROM occurrences where occurrence < 30 INTO OUTFILE '/users/kpi/Documents/work/java/nsi-minlog/performance/data/usedCpr-small_0-30.csv' FIELDS TERMINATED BY ','  ENCLOSED BY '"' LINES TERMINATED BY '\n';
SELECT cprNrBorger FROM occurrences where 200 <= occurrence AND occurrence <= 300 INTO OUTFILE '/users/kpi/Documents/work/java/nsi-minlog/performance/data/usedCpr-small_200-300.csv' FIELDS TERMINATED BY ','  ENCLOSED BY '"' LINES TERMINATED BY '\n';
SELECT cprNrBorger FROM occurrences where 2000 <= occurrence AND occurrence <= 3000 INTO OUTFILE '/users/kpi/Documents/work/java/nsi-minlog/performance/data/usedCpr-small_2000-3000.csv' FIELDS TERMINATED BY ','  ENCLOSED BY '"' LINES TERMINATED BY '\n';
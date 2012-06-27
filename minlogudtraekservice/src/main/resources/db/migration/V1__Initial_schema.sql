CREATE TABLE LogEntry (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `regKode` varchar(36) not null,
  `cprNrBorger` varchar(10) not null,
  `bruger` varchar(20),
  `ansvarlig` varchar(20),
  `orgUsingID` varchar(25),
  `systemName` varchar(25),
  `handling` varchar(75),
  `sessionId` varchar(46),
  `tidspunkt` datetime not null,
  PRIMARY KEY (`id`)
);

CREATE INDEX log_cpr_and_timestamp_index ON LogEntry (`cprNrBorger`, `tidspunkt`) USING BTREE;

CREATE TABLE `status` (
	`id` int NOT NULL,
	`lastUpdated` datetime not null,
	PRIMARY KEY (`id`)
);


CREATE TABLE `whitelist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `legal_cvr` varchar(255) NOT NULL,

  PRIMARY KEY(`id`)
);
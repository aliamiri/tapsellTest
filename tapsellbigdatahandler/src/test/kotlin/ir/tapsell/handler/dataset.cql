CREATE KEYSPACE IF NOT EXISTS tapsellkeyspace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

DROP TABLE IF EXISTS tapsellkeyspace.AdEvent;
DROP TABLE IF EXISTS tapsellkeyspace.MaliciousEvent;

CREATE TABLE IF NOT EXISTS tapsellkeyspace.MaliciousEvent (
    requestId text,
    adId text,
    adTitle text,
    advertiserCost double,
    appId text,
    appTitle text,
    impressionTime double,
    count double,
    PRIMARY KEY (requestId)
);


CREATE TABLE IF NOT EXISTS tapsellkeyspace.AdEvent (
    requestId text,
    adId text,
    adTitle text,
    advertiserCost double,
    appId text,
    appTitle text,
    impressionTime double,
    clickTime double,
    PRIMARY KEY (requestId)
);


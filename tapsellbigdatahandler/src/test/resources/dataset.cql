CREATE KEYSPACE IF NOT EXISTS tapsellspace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

DROP TABLE IF EXISTS tapsellspace.AdEvent;
DROP TABLE IF EXISTS tapsellspace.MaliciousEvent;

CREATE TABLE IF NOT EXISTS tapsellspace.MaliciousEvent (
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


CREATE TABLE IF NOT EXISTS tapsellspace.AdEvent (
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


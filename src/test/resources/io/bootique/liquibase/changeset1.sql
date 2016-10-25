--liquibase formatted sql

--changeset bootique:1
CREATE TABLE A (
    ID INTEGER  NOT NULL,
    NAME VARCHAR (200),
    PRIMARY KEY (ID)
);

INSERT INTO A VALUES (1, 'AA');
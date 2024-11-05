## 3.0-M5

* #34 Upgrade to Liquibase 4.x

## 2.0.M1

* #31 snakeyaml version conflict with Jackson 
* #32 upgrade to liquibase 3.9.0

## 1.1

* #30 DropAll does not work in shadowed JAR / upgrade LB to 3.6.3

## 1.0

## 1.0.RC1

* #26 Cleaning up APIs deprecated since <= 0.25
* #27 Upgrade to the latest Liquibase - 3.6.0 #27

## 0.25

* #22 Specify the default schema `--defaultSchemaName=<schema>` 
* #25 Upgrade to bootique-modules-parent 0.8

## 0.13

* #7 Guess default DataSource if only one DataSource is available
* #14 ChangelogSyncSqlCommand got lost
* #16 Namespace the commands 
* #18 Add Liquibase context to control which changeSets will be executed in migration run 
* #15 Add "drop-all" command
* #23 Upgrade to BQ 0.23 

## 0.12

* #12 Upgrade to Bootique 0.22, replace contribution API with "extend", document config

## 0.11

* #4 Replace 'changeLog' with 'changeLogs' and map Strings to ResourceFactory
* #8 Upgrade to the latest Liquibase 3.5.3
* #9 Upgrades to latest BQ, JDBC, LB
* #10 Contributing change logs via DI
* #11 Add liquibase:clearCheckSums command.

## 0.10

* #6  Upgrade to Bootique 0.20

## 0.9

* #5 Move to io.bootique namespace

## 0.8

* #3 Upgrade Bootique to 0.12

## 0.7

* #1 Bridge Liquibase internal logger to SLF4J
* #2 Start publishing Bootique to Maven central repo

[![Build Status](https://travis-ci.org/bootique/bootique-liquibase.svg)](https://travis-ci.org/bootique/bootique-liquibase)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.bootique.liquibase/bootique-liquibase/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.bootique.liquibase/bootique-liquibase/)

# bootique-liquibase

Provides [Liquibase](http://liquibase.org) integration with [Bootique](http://bootique.io).

*For additional help/questions about this example send a message to
[Bootique forum](https://groups.google.com/forum/#!forum/bootique-user).*
   
## Prerequisites
      
    * Java 1.8 or newer.
    * Apache Maven.
      
# Setup

## Add bootique-liquibase to your build tool
**Maven**
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.bootique.bom</groupId>
            <artifactId>bootique-bom</artifactId>
            <version>0.25-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependency>
    <groupId>io.bootique.liquibase</groupId>
    <artifactId>bootique-liquibase</artifactId>
</dependency>
```
## Available commands

```
OPTIONS
      --catalog=catalog_name
           Catalog against which 'liquibase dropAll' will be executed.

      --config=yaml_location
           Specifies YAML config location, which can be a file path or a URL.

      -h, --help
           Prints this message.

      -H, --help-config
           Prints information about application modules and their configuration
           options.

      --lb-changelog-sync
           Mark all changes as executed in the database.

      --lb-changelog-sync-sql
           Writes SQL to mark all changes as executed in the database to STDOUT.

      --lb-clear-check-sums
           Clears all checksums in the current changelog, so they will be
           recalculated next update.

      -x [val], --lb-context[=val]
           Specifies Liquibase context to control which changeSets will be
           executed in migration run.

      -d [val], --lb-default-schema[=val]
           Specifies the default schema to use for managed database objects and
           for Liquibase control tables.

      --lb-drop-all
           Drops all database objects in the configured schema(s). Note that
           functions, procedures and packages are not dropped.

      -u, --lb-update
           Updates DB with available migrations

      -v, --lb-validate
           Checks the changelog for errors.

      -s schema_name, --schema=schema_name
           Schema against which 'liquibase dropAll' will be executed.
```

## Example Project

[bootique-liquibase-demo](https://github.com/bootique-examples/bootique-liquibase-demo)


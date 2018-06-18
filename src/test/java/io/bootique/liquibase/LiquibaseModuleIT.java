/**
 *  Licensed to ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.liquibase;

import io.bootique.BQRuntime;
import io.bootique.command.CommandOutcome;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.Table;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LiquibaseModuleIT {

    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testMigration_SingleSet() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migrations1.yml", "-u", "-d", "target/derby/migrations1")
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        assertTrue(result.isSuccess());

        Table a = DatabaseChannel.get(runtime).newTable("A").columnNames("ID", "NAME").build();
        Object[] row = a.selectOne();
        assertEquals(1, row[0]);
        assertEquals("AA", row[1]);

        a.matcher().assertOneMatch();

        // rerun....
        runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migrations1.yml", "-u", "-d", "target/derby/migrations1")
                .autoLoadModules()
                .createRuntime();

        result = runtime.run();
        assertTrue(result.isSuccess());

        a.matcher().assertOneMatch();
    }

    @Test
    public void testMigration_MultipleSets() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migrations2.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        assertTrue(result.isSuccess());

        Table a = DatabaseChannel.get(runtime).newTable("A").columnNames("ID", "NAME").build();

        Map<Object, Object[]> rowMap = a.selectAsMap("ID");

        assertEquals(2, rowMap.size());

        Object[] rowA = rowMap.get(1);
        assertEquals("AA", rowA[1]);

        Object[] rowB = rowMap.get(2);
        assertEquals("BB", rowB[1]);

        // rerun....
        runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migrations2.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        result = runtime.run();
        assertTrue(result.isSuccess());

        a.matcher().assertMatches(2);
    }

    @Test
    public void testMigration_MultipleSetsViaYaml() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migrations3.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        assertTrue(result.isSuccess());

        Table a = DatabaseChannel.get(runtime).newTable("A").columnNames("ID", "NAME").build();

        Map<Object, Object[]> rowMap = a.selectAsMap("ID");

        assertEquals(2, rowMap.size());

        Object[] rowA = rowMap.get(1);
        assertEquals("AA", rowA[1]);

        Object[] rowB = rowMap.get(2);
        assertEquals("BB", rowB[1]);

        // rerun....
        runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migrations3.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        result = runtime.run();
        assertTrue(result.isSuccess());

        a.matcher().assertMatches(2);
    }

    @Test
    public void testMigration_MultipleSetsContribution() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migrations4.yml", "-u")
                .autoLoadModules()
                .module(b -> LiquibaseModule.extend(b)
                        .addChangeLog("classpath:io/bootique/liquibase/changeset1.sql")
                        .addChangeLog("classpath:io/bootique/liquibase/changeset2.sql"))
                .createRuntime();

        CommandOutcome result = runtime.run();
        assertTrue(result.isSuccess());

        Table a = DatabaseChannel.get(runtime).newTable("A").columnNames("ID", "NAME").build();

        Map<Object, Object[]> rowMap = a.selectAsMap("ID");

        assertEquals(2, rowMap.size());

        Object[] rowA = rowMap.get(1);
        assertEquals("AA", rowA[1]);

        Object[] rowB = rowMap.get(2);
        assertEquals("BB", rowB[1]);

        // rerun....
        runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migrations3.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        result = runtime.run();
        assertTrue(result.isSuccess());

        a.matcher().assertMatches(2);
    }

    @Test
    public void testMigration_YamlOverridesDI() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migrations3.yml", "-u")
                .autoLoadModules()
                .module(b -> LiquibaseModule.extend(b)
                        .addChangeLog("classpath:io/bootique/liquibase/changeset1.sql")
                        .addChangeLog("classpath:io/bootique/liquibase/changeset2.sql")
                        .addChangeLog("classpath:io/bootique/liquibase/changeset3.sql"))
                .createRuntime();

        CommandOutcome result = runtime.run();
        assertTrue(result.isSuccess());

        Table a = DatabaseChannel.get(runtime).newTable("A").columnNames("ID", "NAME").build();

        Map<Object, Object[]> rowMap = a.selectAsMap("ID");

        assertEquals(2, rowMap.size());

        Object[] rowA = rowMap.get(1);
        assertEquals("AA", rowA[1]);

        Object[] rowB = rowMap.get(2);
        assertEquals("BB", rowB[1]);

        // rerun....
        runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migrations3.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        result = runtime.run();
        assertTrue(result.isSuccess());

        a.matcher().assertMatches(2);
    }

    @Test
    public void testDefaultDataSource() throws SQLException {

        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/noconfig.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        assertTrue(result.isSuccess());

        try (Connection c = DatabaseChannel.get(runtime).getConnection();) {
            DatabaseMetaData md = c.getMetaData();
            assertEquals("jdbc:derby:target/derby/bqjdbc_noconfig", md.getURL());
        }
    }


    @Test
    public void testMigration_Context() throws SQLException {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migration_context.yml", "-u", "-x", "test", "-x", "prod")
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        assertTrue(result.isSuccess());

        Table a = DatabaseChannel.get(runtime).newTable("A").columnNames("ID", "CONTEXT").build();
        List<Object[]> rows = a.select();
        assertEquals("1", rows.get(0)[0]);
        assertEquals("2", rows.get(1)[0]);
        assertEquals("3", rows.get(2)[0]);
        assertEquals("4", rows.get(3)[0]);
        assertEquals("5", rows.get(4)[0]);

        assertEquals("prod", rows.get(0)[1]);
        assertEquals("test", rows.get(1)[1]);
        assertEquals("no context", rows.get(2)[1]);
        assertEquals("test and prod", rows.get(3)[1]);
        assertEquals("test or prod", rows.get(4)[1]);

        a.matcher().assertMatches(5);

        // rerun....
        runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migration_context.yml", "-u", "-x", "test", "-x", "prod")
                .autoLoadModules()
                .createRuntime();

        result = runtime.run();
        assertTrue(result.isSuccess());

        a.matcher().assertMatches(5);
    }

    @Test
    public void testMigration_NoContext() throws SQLException {
        //not specify a context when you run the migrator, ALL contexts will be run
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migration_context.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        assertTrue(result.isSuccess());

        Table a = DatabaseChannel.get(runtime).newTable("A").columnNames("ID", "CONTEXT").build();
        a.matcher().assertMatches(7);

        // rerun....
        runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migration_context.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        result = runtime.run();
        assertTrue(result.isSuccess());

        a.matcher().assertMatches(7);
    }

    @Test
    public void testMigration_UnknownContext() throws SQLException {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migration_context.yml", "-u", "-x", "unknown")
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        assertTrue(result.isSuccess());

        Table a = DatabaseChannel.get(runtime).newTable("A").columnNames("ID", "CONTEXT").build();
        a.matcher().assertMatches(3);
        List<Object[]> rows = a.select();
        assertEquals("3", rows.get(0)[0]);
        assertEquals("6", rows.get(1)[0]);
        assertEquals("7", rows.get(2)[0]);

        assertEquals("no context", rows.get(0)[1]);
        assertEquals("!test", rows.get(1)[1]);
        assertEquals("!test and !prod", rows.get(2)[1]);

        // rerun....
        runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migration_context.yml", "-u", "-x", "unknown")
                .autoLoadModules()
                .createRuntime();

        result = runtime.run();
        assertTrue(result.isSuccess());

        a.matcher().assertMatches(3);
    }

    @Test
    public void testMigration_Schema() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migration_schema.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        assertTrue(result.isSuccess());

        Table y = DatabaseChannel.get(runtime).newTable("Y").columnNames("ID", "SCHEMA").build();
        Object[] row = y.selectOne();
        assertEquals("1", row[0]);
        assertEquals("APP", row[1]);
        y.matcher().assertOneMatch();

        Table x = DatabaseChannel.get(runtime).newTable("X").columnNames("ID", "SCHEMA").build();
        try {
            x.selectOne();
            fail("Exception expected");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof SQLSyntaxErrorException);
        }

        // rerun....
        runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migration_schema.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        result = runtime.run();
        assertTrue(result.isSuccess());

        y.matcher().assertOneMatch();
    }

    @Test
    public void testMigration_DefaultSchema() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migration_schema.yml", "-u", "-d", "test")
                .autoLoadModules()
                .createRuntime();

        LiquibaseRunner runner = runtime.getInstance(LiquibaseRunner.class);
        runner.runWithLiquibase(lb -> {
            assertEquals("TEST", lb.getDatabase().getDefaultSchemaName());
            assertEquals("TEST", lb.getDatabase().getLiquibaseSchemaName());

            return lb;
        });
    }

}

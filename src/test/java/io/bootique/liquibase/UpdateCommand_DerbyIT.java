/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.liquibase;

import io.bootique.BQRuntime;
import io.bootique.command.CommandOutcome;
import io.bootique.di.BQModule;
import io.bootique.jdbc.junit5.DbTester;
import io.bootique.jdbc.junit5.Table;
import io.bootique.jdbc.liquibase.LiquibaseRunner;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import io.bootique.junit5.TestRuntumeBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class UpdateCommand_DerbyIT {

    @BQTestTool
    final DbTester db = DbTester.derbyDb();

    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory();

    private CommandOutcome run(DbTester db, String[] cli, BQModule... extras) {
        TestRuntumeBuilder builder = testFactory
                .app(cli)
                .autoLoadModules()
                .module(db.moduleWithTestDataSource("test"));

        for (BQModule e : extras) {
            builder.module(e);
        }

        return builder.run();
    }

    @Test
    public void testSingleSet() {

        String[] cli = {"-c", "classpath:io/bootique/liquibase/migrations1.yml", "-u", "-d", "target/derby/migrations1"};
        CommandOutcome migrate1 = run(db, cli);

        assertTrue(migrate1.isSuccess());

        Table a = db.getTable("A");
        a.matcher().assertOneMatch();

        Object[] row = a.selectOne();
        assertEquals(1, row[0]);
        assertEquals("AA", row[1]);

        // rerun....
        CommandOutcome migrate2 = run(db, cli);
        assertTrue(migrate2.isSuccess());
        a.matcher().assertOneMatch();
    }

    @Test
    public void testMultipleSets() {

        String[] cli = {"-c", "classpath:io/bootique/liquibase/migrations2.yml", "-u"};

        CommandOutcome migrate1 = run(db, cli);
        assertTrue(migrate1.isSuccess());

        Map<Object, Object[]> rowMap = db.getTable("A").selectAsMap("ID");
        assertEquals(2, rowMap.size());

        Object[] rowA = rowMap.get(1);
        assertEquals("AA", rowA[1]);

        Object[] rowB = rowMap.get(2);
        assertEquals("BB", rowB[1]);

        // rerun....
        CommandOutcome migrate2 = run(db, cli);
        assertTrue(migrate2.isSuccess());
        db.getTable("A").matcher().assertMatches(2);
    }

    @Test
    public void testMultipleSetsViaYaml() {
        String[] cli = {"-c", "classpath:io/bootique/liquibase/migrations3.yml", "-u"};

        CommandOutcome migrate1 = run(db, cli);
        assertTrue(migrate1.isSuccess());

        Table a = db.getTable("A");

        Map<Object, Object[]> rowMap = a.selectAsMap("ID");

        assertEquals(2, rowMap.size());

        Object[] rowA = rowMap.get(1);
        assertEquals("AA", rowA[1]);

        Object[] rowB = rowMap.get(2);
        assertEquals("BB", rowB[1]);

        // rerun....
        CommandOutcome migrate2 = run(db, cli);
        assertTrue(migrate2.isSuccess());
        a.matcher().assertMatches(2);
    }

    @Test
    public void testMultipleSetsContribution() {
        String[] cli = {"-u"};
        CommandOutcome migrate1 = run(db, cli, b -> LiquibaseModule.extend(b)
                .addChangeLog("classpath:io/bootique/liquibase/changeset1.sql")
                .addChangeLog("classpath:io/bootique/liquibase/changeset2.sql"));

        assertTrue(migrate1.isSuccess());

        Table a = db.getTable("A");
        Map<Object, Object[]> rowMap = a.selectAsMap("ID");

        assertEquals(2, rowMap.size());

        Object[] rowA = rowMap.get(1);
        assertEquals("AA", rowA[1]);

        Object[] rowB = rowMap.get(2);
        assertEquals("BB", rowB[1]);

        // rerun....
        CommandOutcome migrate2 = run(db, cli);
        assertTrue(migrate2.isSuccess());
        a.matcher().assertMatches(2);
    }

    @Test
    public void testYamlOverridesDI() {

        String[] cli = {"-c", "classpath:io/bootique/liquibase/migrations3.yml", "-u"};
        CommandOutcome migrate1 = run(db, cli, b -> LiquibaseModule.extend(b)
                .addChangeLog("classpath:io/bootique/liquibase/changeset1.sql")
                .addChangeLog("classpath:io/bootique/liquibase/changeset2.sql")
                .addChangeLog("classpath:io/bootique/liquibase/changeset3.sql"));

        assertTrue(migrate1.isSuccess());

        Table a = db.getTable("A");

        Map<Object, Object[]> rowMap = a.selectAsMap("ID");

        assertEquals(2, rowMap.size());

        Object[] rowA = rowMap.get(1);
        assertEquals("AA", rowA[1]);

        Object[] rowB = rowMap.get(2);
        assertEquals("BB", rowB[1]);

        // rerun....
        CommandOutcome migrate2 = run(db, cli);
        assertTrue(migrate2.isSuccess());
        a.matcher().assertMatches(2);
    }

    @Test
    public void testDefaultDataSource() {

        String[] cli = {"-c", "classpath:io/bootique/liquibase/migrations1_implicit_ds.yml", "-u"};
        CommandOutcome migrate1 = run(db, cli);
        assertTrue(migrate1.isSuccess());

        Table a = db.getTable("A");
        a.matcher().assertOneMatch();

        Object[] row = a.selectOne();
        assertEquals(1, row[0]);
        assertEquals("AA", row[1]);

        // rerun....
        CommandOutcome migrate2 = run(db, cli);
        assertTrue(migrate2.isSuccess());
        a.matcher().assertOneMatch();
    }

    @Test
    public void testContext() {
        String[] cli = {"-c", "classpath:io/bootique/liquibase/migration_context.yml", "-u", "-x", "test", "-x", "prod"};
        CommandOutcome migrate1 = run(db, cli);
        assertTrue(migrate1.isSuccess());

        Table a = db.getTable("A");
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
        CommandOutcome migrate2 = run(db, cli);
        assertTrue(migrate2.isSuccess());
        a.matcher().assertMatches(5);
    }

    @Test
    public void testNoExplicitContext() {

        // no explicit context, so all contexts must be run

        String[] cli = {"-c", "classpath:io/bootique/liquibase/migration_context.yml", "-u"};
        CommandOutcome migrate1 = run(db, cli);
        assertTrue(migrate1.isSuccess());

        Table a = db.getTable("A");
        a.matcher().assertMatches(7);

        // rerun....
        CommandOutcome migrate2 = run(db, cli);
        assertTrue(migrate2.isSuccess());
        a.matcher().assertMatches(7);
    }

    @Test
    public void testUnknownContext() {
        String[] cli = {"-c", "classpath:io/bootique/liquibase/migration_context.yml", "-u", "-x", "unknown"};
        CommandOutcome migrate1 = run(db, cli);
        assertTrue(migrate1.isSuccess());

        Table a = db.getTable("A");
        a.matcher().assertMatches(3);
        List<Object[]> rows = a.select();
        assertEquals("3", rows.get(0)[0]);
        assertEquals("6", rows.get(1)[0]);
        assertEquals("7", rows.get(2)[0]);

        assertEquals("no context", rows.get(0)[1]);
        assertEquals("!test", rows.get(1)[1]);
        assertEquals("!test and !prod", rows.get(2)[1]);

        // rerun....
        CommandOutcome migrate2 = run(db, cli);
        assertTrue(migrate2.isSuccess());
        a.matcher().assertMatches(3);
    }

    @Test
    public void testSchema() {
        String[] cli = {"-c", "classpath:io/bootique/liquibase/migration_schema.yml", "-u"};
        CommandOutcome migrate1 = run(db, cli);
        assertTrue(migrate1.isSuccess());

        Table y = db.getTable("Y");
        Object[] row = y.selectOne();
        assertEquals("1", row[0]);
        assertEquals("APP", row[1]);
        y.matcher().assertOneMatch();

        Table x = db.getTable("X");
        assertThrows(Exception.class, () -> x.selectOne());

        // rerun....
        CommandOutcome migrate2 = run(db, cli);
        assertTrue(migrate2.isSuccess());
        y.matcher().assertOneMatch();
    }

    @Test
    public void testDefaultSchema() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migration_schema.yml", "-u", "-d", "test")
                .module(db.moduleWithTestDataSource("test"))
                .autoLoadModules()
                .createRuntime();

        LiquibaseRunner runner = runtime.getInstance(LiquibaseRunner.class);
        runner.run(lb -> {
            assertEquals("TEST", lb.getDatabase().getDefaultSchemaName());
            assertEquals("TEST", lb.getDatabase().getLiquibaseSchemaName());
        });
    }
}

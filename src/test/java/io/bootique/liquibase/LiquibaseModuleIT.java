package io.bootique.liquibase;

import io.bootique.BQRuntime;
import io.bootique.command.CommandOutcome;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.Table;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LiquibaseModuleIT {

    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    /**
     * Testing deprecated 'changeSet' key.
     *
     * @deprecated since 0.11
     */
    @Test
    @Deprecated
    public void testLegacy_Migration() {

        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/deprecated_migrations1.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        Assert.assertTrue(result.isSuccess());

        Table a = DatabaseChannel.get(runtime).newTable("A").columnNames("ID", "NAME").build();
        Object[] row = a.selectOne();
        assertEquals(1, row[0]);
        assertEquals("AA", row[1]);
        assertEquals(1, a.getRowCount());

        // rerun....
        runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/deprecated_migrations1.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        result = runtime.run();
        Assert.assertTrue(result.isSuccess());

        assertEquals(1, a.getRowCount());
    }

    @Test
    public void testMigration_SingleSet() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migrations1.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        Assert.assertTrue(result.isSuccess());

        Table a = DatabaseChannel.get(runtime).newTable("A").columnNames("ID", "NAME").build();
        Object[] row = a.selectOne();
        assertEquals(1, row[0]);
        assertEquals("AA", row[1]);
        assertEquals(1, a.getRowCount());

        // rerun....
        runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migrations1.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        result = runtime.run();
        Assert.assertTrue(result.isSuccess());

        assertEquals(1, a.getRowCount());
    }

    @Test
    public void testMigration_MultipleSets() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migrations2.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        Assert.assertTrue(result.isSuccess());

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
        Assert.assertTrue(result.isSuccess());

        assertEquals(2, a.getRowCount());
    }

    @Test
    public void testMigration_MultipleSetsViaYaml() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migrations3.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        Assert.assertTrue(result.isSuccess());

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
        Assert.assertTrue(result.isSuccess());

        assertEquals(2, a.getRowCount());
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
        Assert.assertTrue(result.isSuccess());

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
        Assert.assertTrue(result.isSuccess());

        assertEquals(2, a.getRowCount());
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
        Assert.assertTrue(result.isSuccess());

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
        Assert.assertTrue(result.isSuccess());

        assertEquals(2, a.getRowCount());
    }

    @Test
    public void testDefaultDataSource() throws SQLException {

        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/noconfig.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        Assert.assertTrue(result.isSuccess());

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
        Assert.assertTrue(result.isSuccess());

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

        assertEquals(5, a.getRowCount());

        // rerun....
        runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migration_context.yml", "-u", "-x", "test", "-x", "prod")
                .autoLoadModules()
                .createRuntime();

        result = runtime.run();
        Assert.assertTrue(result.isSuccess());

        assertEquals(5, a.getRowCount());
    }

    @Test
    public void testMigration_NoContext() throws SQLException {
        //not specify a context when you run the migrator, ALL contexts will be run
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migration_context.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        Assert.assertTrue(result.isSuccess());

        Table a = DatabaseChannel.get(runtime).newTable("A").columnNames("ID", "CONTEXT").build();
        assertEquals(7, a.getRowCount());

        // rerun....
        runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migration_context.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        result = runtime.run();
        Assert.assertTrue(result.isSuccess());

        assertEquals(7, a.getRowCount());
    }

    @Test
    public void testMigration_UnknownContext() throws SQLException {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migration_context.yml", "-u", "-x", "unknown")
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        Assert.assertTrue(result.isSuccess());

        Table a = DatabaseChannel.get(runtime).newTable("A").columnNames("ID", "CONTEXT").build();
        assertEquals(3, a.getRowCount());
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
        Assert.assertTrue(result.isSuccess());

        assertEquals(3, a.getRowCount());
    }
}

package io.bootique.liquibase;

import io.bootique.command.CommandOutcome;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.Table;
import io.bootique.test.BQTestRuntime;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
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

        BQTestRuntime runtime = testFactory
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
        BQTestRuntime runtime = testFactory
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
        BQTestRuntime runtime = testFactory
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
        BQTestRuntime runtime = testFactory
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
        BQTestRuntime runtime = testFactory
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
        BQTestRuntime runtime = testFactory
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

        BQTestRuntime runtime = testFactory
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

}

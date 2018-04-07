package io.bootique.liquibase;

import io.bootique.BQRuntime;
import io.bootique.command.CommandOutcome;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.Table;
import io.bootique.test.junit.BQTestFactory;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.MySQLContainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LiquibaseMySQLIT {

    private static final String DB_NAME = "SCHEMA-X";
    private static final String USER_NAME = "bar";
    private static final String PASSWORD = "baz";

    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    @ClassRule
    public static MySQLContainer MYSQL = (MySQLContainer) new MySQLContainer()
            .withDatabaseName(DB_NAME)
            .withUsername(USER_NAME)
            .withPassword(PASSWORD)
            .withEnv("MYSQL_ROOT_HOST", "%");

    private static String dbUrl() {
        return String.format("jdbc:mysql://%s:%s/%s?TC_INITSCRIPT=%s",
                MYSQL.getContainerIpAddress(),
                MYSQL.getMappedPort(MySQLContainer.MYSQL_PORT),
                DB_NAME,
                "io/bootique/liquibase/init_mysql.sql");
    }

    @Test
    public void testMigration() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migration_mySQL.yml", "-u")
                .property("bq.jdbc.myds.url", dbUrl())
                .property("bq.jdbc.myds.username", USER_NAME)
                .property("bq.jdbc.myds.password", PASSWORD)
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        assertTrue(result.isSuccess());

        Table x = DatabaseChannel.get(runtime).newTable("X").columnNames("ID", "SCHEMA").build();
        Object[] rowX = x.selectOne();
        assertEquals("1", rowX[0]);
        assertEquals("testX", rowX[1]);
        x.matcher().assertOneMatch();

        Table y = DatabaseChannel.get(runtime).newTable("Y").columnNames("ID", "SCHEMA").build();
        Object[] rowY = y.selectOne();
        assertEquals("1", rowY[0]);
        assertEquals("testY", rowY[1]);
        y.matcher().assertOneMatch();
    }
}

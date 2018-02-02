package io.bootique.liquibase;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.inject.Injector;
import io.bootique.BQRuntime;
import io.bootique.command.CommandOutcome;
import io.bootique.jdbc.JdbcModule;
import io.bootique.jdbc.managed.ManagedDataSourceSupplier;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.Table;
import io.bootique.jdbc.tomcat.TomcatManagedDataSourceFactory;
import io.bootique.test.junit.BQTestFactory;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.MySQLContainer;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LiquibaseMySQLIT {
    private static final String DB_NAME = "SCHEMA-X";
    private static final String USER_NAME = "bar";
    private static final String PASSWORD = "baz";

    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    @ClassRule
    public static MySQLContainer mysql = (MySQLContainer) new MySQLContainer()
            .withDatabaseName(DB_NAME)
            .withUsername(USER_NAME)
            .withPassword(PASSWORD)
            .withEnv("MYSQL_ROOT_HOST", "%");

    @Test
    public void testMigration() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migration_mySQL.yml", "-u")
                .module(b -> JdbcModule.extend(b).addFactoryType(MySQLDataSourceFactory.class))
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        assertTrue(result.isSuccess());

        Table x = DatabaseChannel.get(runtime).newTable("X").columnNames("ID", "SCHEMA").build();
        Object[] rowX = x.selectOne();
        assertEquals("1", rowX[0]);
        assertEquals("testX", rowX[1]);
        assertEquals(1, x.getRowCount());

        Table y = DatabaseChannel.get(runtime).newTable("Y").columnNames("ID", "SCHEMA").build();
        Object[] rowY = y.selectOne();
        assertEquals("1", rowY[0]);
        assertEquals("testY", rowY[1]);
        assertEquals(1, y.getRowCount());
    }

    @JsonTypeName("mysql")
    public static class MySQLDataSourceFactory extends TomcatManagedDataSourceFactory {

        public MySQLDataSourceFactory() {
            super();
        }

        @Override
        public Optional<ManagedDataSourceSupplier> create(Injector injector) {
            Supplier<DataSource> startup = () -> {

                validate();

                PoolConfiguration poolConfig = toConfiguration();
                poolConfig.setUrl("jdbc:mysql://"
                        + mysql.getContainerIpAddress()
                        + ":" + mysql.getMappedPort(MySQLContainer.MYSQL_PORT)
                        + "/" + DB_NAME
                        + "?" + "TC_INITSCRIPT=io/bootique/liquibase/init_mysql.sql");
                poolConfig.setUsername(USER_NAME);
                poolConfig.setPassword(PASSWORD);

                org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource(poolConfig);
                try {
                    dataSource.createPool();
                } catch (Exception e) {
                    throw new RuntimeException("Error creating DataSource", e);
                }

                return dataSource;
            };

            Consumer<DataSource> shutdown = ds -> ((org.apache.tomcat.jdbc.pool.DataSource) ds).close();

            return Optional.of(new ManagedDataSourceSupplier(getUrl(), startup, shutdown));
        }
    }
}

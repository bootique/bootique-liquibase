package io.bootique.liquibase;

import io.bootique.cli.Cli;
import io.bootique.liquibase.database.DerbyDatabase;
import io.bootique.resource.ResourceFactory;
import liquibase.ContextExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.resource.ResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

public class LiquibaseRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseRunner.class);

    protected Collection<ResourceFactory> changeLogs;
    protected DataSource dataSource;
    protected Cli cli;
    private SLFLiquibaseAdapter loggerAdapter;

    public LiquibaseRunner(Collection<ResourceFactory> changeLogs,
                           DataSource dataSource,
                           Cli cli) {
        this.changeLogs = Objects.requireNonNull(changeLogs);
        this.dataSource = Objects.requireNonNull(dataSource);
        this.cli = cli;

        Logger lbLogger = LoggerFactory.getLogger(Liquibase.class);
        this.loggerAdapter = new SLFLiquibaseAdapter(lbLogger);

        setupLogging();
    }

    public <T> T runWithLiquibase(Function<Liquibase, T> op) {

        Liquibase lb = createLiquibase();
        try {
            return op.apply(lb);
        } finally {
            closeLiquibase(lb);
        }
    }

    protected Liquibase createLiquibase() {
        ResourceAccessor resourceAccessor = new ResourceFactoryAccessor();

        try {
            Database liquibaseDB = createDatabase(dataSource.getConnection(), resourceAccessor);
            DatabaseChangeLog changeLog = createDatabaseChangeLog(liquibaseDB, resourceAccessor);
            return new Liquibase(changeLog, resourceAccessor, liquibaseDB);
        } catch (SQLException | LiquibaseException e) {
            throw new RuntimeException("Error creating liquibase", e);
        }
    }

    protected void setupLogging() {
        LogFactory.setInstance(new LogFactory() {
            @Override
            public liquibase.logging.Logger getLog(String name) {
                return loggerAdapter;
            }
        });
    }

    protected DatabaseChangeLog createDatabaseChangeLog(Database database, ResourceAccessor resourceAccessor) {
        DatabaseChangeLog changeLog = new DatabaseChangeLog();
        changeLog.setChangeLogParameters(new ChangeLogParameters(database));

        changeLogs.forEach(cl -> {
            try {
                LOGGER.info("Including change log: '{}'", cl.getResourceId());
                changeLog.include(cl.getResourceId(), false, resourceAccessor, new ContextExpression());
            } catch (LiquibaseException e) {
                throw new RuntimeException("Error configuring Liquibase", e);
            }
        });

        return changeLog;
    }


    protected Database createDatabase(Connection c, ResourceAccessor resourceAccessor) throws DatabaseException {

        DatabaseConnection liquibaseConnection = new JdbcConnection(c);
        DatabaseFactory databaseFactory = DatabaseFactory.getInstance();

        // expand factory with our extensions....
        databaseFactory.register(new DerbyDatabase());

        Database database = databaseFactory.findCorrectDatabaseImplementation(liquibaseConnection);
        // set default schema
        String defaultSchema = cli.optionString(LiquibaseModule.DEFAULT_SCHEMA_OPTION);
        if (defaultSchema != null) {
            database.setDefaultSchemaName(defaultSchema);
        }

        return database;
    }

    protected void closeLiquibase(Liquibase lb) {
        if (lb.getDatabase() != null) {
            try {
                lb.getDatabase().close();
            } catch (DatabaseException e) {
                LOGGER.info("Error closing Liquibase, ignored", e);
            }
        }
    }

}

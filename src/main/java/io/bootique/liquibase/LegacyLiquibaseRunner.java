package io.bootique.liquibase;

import io.bootique.cli.Cli;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;

/**
 * A LiquibaseRunner for a single classpath-based changelog.
 *
 * @since 0.11
 */
public class LegacyLiquibaseRunner extends LiquibaseRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyLiquibaseRunner.class);

    private String changeLog;

    public LegacyLiquibaseRunner(String changeLog,
                                 DataSource dataSource,
                                 Cli cli) {
        super(Collections.emptyList(), dataSource, cli);
        this.changeLog = changeLog;
    }

    @Override
    protected Liquibase createLiquibase() {
        ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
        try {
            Database liquibaseDB = createDatabase(dataSource.getConnection(), resourceAccessor);

            // TODO: other LB settings?

            LOGGER.info("Change log: '{}'", changeLog);

            return new Liquibase(changeLog, resourceAccessor, liquibaseDB);
        } catch (SQLException | LiquibaseException e) {
            throw new RuntimeException("Error creating liquibase", e);
        }
    }
}

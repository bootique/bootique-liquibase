package io.bootique.liquibase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

public class LiquibaseRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseRunner.class);

	private DataSource dataSource;
	private String changeLog;
	private SLFLiquibaseAdapter loggerAdapter;

	public LiquibaseRunner(String changeLog, DataSource dataSource) {
		this.changeLog = changeLog;
		this.dataSource = dataSource;

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

	protected void setupLogging() {
		LogFactory.setInstance(new LogFactory() {
			@Override
			public liquibase.logging.Logger getLog(String name) {
				return loggerAdapter;
			}
		});
	}

	protected Database createDatabase(Connection c, ResourceAccessor resourceAccessor) throws DatabaseException {

		DatabaseConnection liquibaseConnection = new JdbcConnection(c);
		Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(liquibaseConnection);

		// TODO: set default schema?

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

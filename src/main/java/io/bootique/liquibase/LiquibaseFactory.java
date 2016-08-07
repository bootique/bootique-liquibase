package io.bootique.liquibase;

import io.bootique.jdbc.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Objects;

public class LiquibaseFactory {

	private String datasource;
	private String changeLog;

	public void setDatasource(String datasource) {
		this.datasource = datasource;
	}

	public void setChangeLog(String changeLog) {
		this.changeLog = changeLog;
	}

	public LiquibaseRunner createRunner(DataSourceFactory dataSourceFactory) {
		Objects.requireNonNull(datasource, "'datasource' property is null");
		DataSource ds = dataSourceFactory.forName(datasource);
		return new LiquibaseRunner(changeLog, ds);
	}

}

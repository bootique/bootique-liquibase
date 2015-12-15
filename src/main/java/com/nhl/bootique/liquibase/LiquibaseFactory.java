package com.nhl.bootique.liquibase;

import javax.sql.DataSource;

import com.nhl.bootique.cayenne.datasource.SimpleDataSourceFactory;

public class LiquibaseFactory {

	private SimpleDataSourceFactory datasource;
	private String changeLog;

	public void setDatasource(SimpleDataSourceFactory dataSource) {
		this.datasource = dataSource;
	}

	public void setChangeLog(String changeLog) {
		this.changeLog = changeLog;
	}

	public LiquibaseRunner createRunner() {
		DataSource ds = datasource.toDataSource();
		return new LiquibaseRunner(changeLog, ds);
	}

}

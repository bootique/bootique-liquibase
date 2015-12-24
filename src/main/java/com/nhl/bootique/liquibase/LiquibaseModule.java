package com.nhl.bootique.liquibase;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.nhl.bootique.BQBinder;
import com.nhl.bootique.ConfigModule;
import com.nhl.bootique.config.ConfigurationFactory;
import com.nhl.bootique.jdbc.DataSourceFactory;
import com.nhl.bootique.liquibase.command.UpdateCommand;

public class LiquibaseModule extends ConfigModule {

	public LiquibaseModule(String configPrefix) {
		super(configPrefix);
	}

	public LiquibaseModule() {
	}

	@Override
	public void configure(Binder binder) {
		BQBinder.contributeTo(binder).commandTypes(UpdateCommand.class);
	}

	@Provides
	public LiquibaseRunner createRunner(ConfigurationFactory configFactory, DataSourceFactory dataSourceFactory) {
		return configFactory.config(LiquibaseFactory.class, configPrefix).createRunner(dataSourceFactory);
	}
}

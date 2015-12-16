package com.nhl.bootique.liquibase;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.nhl.bootique.BQBinder;
import com.nhl.bootique.FactoryModule;
import com.nhl.bootique.factory.FactoryConfigurationService;
import com.nhl.bootique.liquibase.command.UpdateCommand;

public class LiquibaseModule extends FactoryModule<LiquibaseFactory> {

	public LiquibaseModule(String configPrefix) {
		super(LiquibaseFactory.class, configPrefix);
	}

	public LiquibaseModule() {
		super(LiquibaseFactory.class);
	}

	@Override
	public void configure(Binder binder) {
		BQBinder.contributeTo(binder).commandTypes(UpdateCommand.class);
	}

	@Provides
	public LiquibaseRunner createRunner(FactoryConfigurationService factoryService) {
		return createFactory(factoryService).createRunner();
	}
}

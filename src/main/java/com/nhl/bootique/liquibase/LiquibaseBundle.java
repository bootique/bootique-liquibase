package com.nhl.bootique.liquibase;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.nhl.bootique.BQModule;
import com.nhl.bootique.factory.FactoryConfigurationService;
import com.nhl.bootique.liquibase.command.UpdateCommand;

public class LiquibaseBundle {

	private static final String CONFIG_PREFIX = "liquibase";

	private String configPrefix;

	public static LiquibaseBundle create() {
		return create(CONFIG_PREFIX);
	}

	public static LiquibaseBundle create(String configPrefix) {
		return new LiquibaseBundle(configPrefix);
	}

	public static Module liquibaseModule() {
		return create().module();
	}

	private LiquibaseBundle(String configPrefix) {
		this.configPrefix = configPrefix;
	}

	public Module module() {
		return new LiquibaseModule();
	}

	class LiquibaseModule implements Module {

		@Override
		public void configure(Binder binder) {
			BQModule.bindCommands(binder, UpdateCommand.class);
		}

		@Provides
		public LiquibaseRunner createRunner(FactoryConfigurationService factoryService) {
			return factoryService.factory(LiquibaseFactory.class, configPrefix).createRunner();
		}
	}
}

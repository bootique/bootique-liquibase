package io.bootique.liquibase;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.BQCoreModule;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.liquibase.command.UpdateCommand;

public class LiquibaseModule extends ConfigModule {

	public LiquibaseModule(String configPrefix) {
		super(configPrefix);
	}

	public LiquibaseModule() {
	}

	@Override
	public void configure(Binder binder) {
		BQCoreModule.contributeCommands(binder).addBinding().to(UpdateCommand.class).in(Singleton.class);
	}

	@Provides
	public LiquibaseRunner createRunner(ConfigurationFactory configFactory, DataSourceFactory dataSourceFactory) {
		return configFactory.config(LiquibaseFactory.class, configPrefix).createRunner(dataSourceFactory);
	}
}

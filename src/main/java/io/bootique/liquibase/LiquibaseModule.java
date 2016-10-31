package io.bootique.liquibase;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import io.bootique.BQCoreModule;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.liquibase.annotation.ChangeLogs;
import io.bootique.liquibase.command.UpdateCommand;
import io.bootique.resource.ResourceFactory;

import java.util.Set;

public class LiquibaseModule extends ConfigModule {

    public LiquibaseModule(String configPrefix) {
        super(configPrefix);
    }

    public LiquibaseModule() {
    }

    public static Multibinder<ResourceFactory> contributeChangeLogs(Binder binder) {
        return Multibinder.newSetBinder(binder, ResourceFactory.class, ChangeLogs.class);
    }

    @Override
    public void configure(Binder binder) {
        BQCoreModule.contributeCommands(binder).addBinding().to(UpdateCommand.class).in(Singleton.class);
        contributeChangeLogs(binder);
    }

    @Provides
    public LiquibaseRunner createRunner(ConfigurationFactory configFactory,
                                        DataSourceFactory dataSourceFactory,
                                        @ChangeLogs Set<ResourceFactory> injectedChangeLogs) {
        return configFactory
                .config(LiquibaseFactory.class, configPrefix)
                .createRunner(dataSourceFactory, injectedChangeLogs);
    }
}

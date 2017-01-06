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
import io.bootique.liquibase.command.ChangelogSyncCommand;
import io.bootique.liquibase.command.ChangelogSyncSqlCommand;
import io.bootique.liquibase.command.ClearCheckSumsCommand;
import io.bootique.liquibase.command.UpdateCommand;
import io.bootique.liquibase.command.ValidateCommand;
import io.bootique.resource.ResourceFactory;

import java.util.Set;

import static java.util.Arrays.asList;

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
        asList(
                UpdateCommand.class,
                ValidateCommand.class,
                ChangelogSyncCommand.class,
                ClearCheckSumsCommand.class,
                ChangelogSyncSqlCommand.class
        ).forEach(command ->
                BQCoreModule.contributeCommands(binder).addBinding().to(command).in(Singleton.class));
        contributeChangeLogs(binder);
    }

    @Provides
    @Singleton
    ChangeLogMerger provideLogMerger() {
        return new ChangeLogMerger();
    }

    @Provides
    @Singleton
    LiquibaseRunner provideRunner(ConfigurationFactory configFactory,
                                  DataSourceFactory dataSourceFactory,
                                  ChangeLogMerger changeLogMerger,
                                  @ChangeLogs Set<ResourceFactory> injectedChangeLogs) {

        return configFactory
                .config(LiquibaseFactory.class, configPrefix)
                .createRunner(dataSourceFactory, configChangeLogs
                        -> changeLogMerger.merge(injectedChangeLogs, configChangeLogs));
    }
}

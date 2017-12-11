package io.bootique.liquibase;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import io.bootique.BQCoreModule;
import io.bootique.ConfigModule;
import io.bootique.cli.Cli;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.liquibase.annotation.ChangeLogs;
import io.bootique.liquibase.command.ChangelogSyncCommand;
import io.bootique.liquibase.command.ChangelogSyncSqlCommand;
import io.bootique.liquibase.command.ClearCheckSumsCommand;
import io.bootique.liquibase.command.DropAllCommand;
import io.bootique.liquibase.command.UpdateCommand;
import io.bootique.liquibase.command.ValidateCommand;
import io.bootique.meta.application.OptionMetadata;
import io.bootique.resource.ResourceFactory;

import java.util.Set;

public class LiquibaseModule extends ConfigModule {
    public static final String CONTEXT_OPTION = "lb-context";
    public static final String DEFAULT_SCHEMA_OPTION = "lb-default-schema";

    public LiquibaseModule(String configPrefix) {
        super(configPrefix);
    }

    public LiquibaseModule() {
    }

    /**
     * @param binder DI binder passed to the Module that invokes this method.
     * @return an instance of {@link LiquibaseModuleExtender} that can be used to load custom extensions.
     * @since 0.12
     */
    public static LiquibaseModuleExtender extend(Binder binder) {
        return new LiquibaseModuleExtender(binder);
    }

    /**
     * Returns a Guice {@link Multibinder} to add Liquibase change logs
     *
     * @param binder DI binder passed to the Module that invokes this method.
     * @return returns a {@link Multibinder} for Liquibase change logs.
     * @deprecated since 0.12 call {@link #extend(Binder)} and then call
     * {@link LiquibaseModuleExtender#addChangeLog(ResourceFactory)}.
     */
    @Deprecated
    public static Multibinder<ResourceFactory> contributeChangeLogs(Binder binder) {
        return Multibinder.newSetBinder(binder, ResourceFactory.class, ChangeLogs.class);
    }

    @Override
    public void configure(Binder binder) {

        LiquibaseModule.extend(binder).initAllExtensions();

        BQCoreModule.extend(binder)
                .addCommand(UpdateCommand.class)
                .addCommand(ValidateCommand.class)
                .addCommand(ChangelogSyncSqlCommand.class)
                .addCommand(ClearCheckSumsCommand.class)
                .addCommand(ChangelogSyncCommand.class)
                .addCommand(DropAllCommand.class)
                .addOptions(createContextOption(), createDefaultSchemaOption());
    }

    OptionMetadata createContextOption() {
        return OptionMetadata
                .builder(CONTEXT_OPTION,
                        "Specifies Liquibase context to control which changeSets will be executed in migration run.")
                .shortName('x')
                .valueOptional()
                .build();
    }

    OptionMetadata createDefaultSchemaOption() {
        return OptionMetadata
                .builder(DEFAULT_SCHEMA_OPTION,
                        "Specifies the default schema to use for managed database objects and for Liquibase control tables.")
                .shortName('d')
                .valueOptional()
                .build();
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
                                  @ChangeLogs Set<ResourceFactory> injectedChangeLogs,
                                  Cli cli) {

        return configFactory
                .config(LiquibaseFactory.class, configPrefix)
                .createRunner(dataSourceFactory, configChangeLogs
                                -> changeLogMerger.merge(injectedChangeLogs, configChangeLogs),
                        cli);
    }
}

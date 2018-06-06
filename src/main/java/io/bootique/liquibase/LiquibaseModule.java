/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.liquibase;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
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
import java.util.logging.Level;

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
                .addOptions(createContextOption(), createDefaultSchemaOption())
                // by default turn off Liquibase SQL logging (which is done at INFO level)
                .setLogLevel("liquibase.executor.jvm.JdbcExecutor", Level.WARNING);
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

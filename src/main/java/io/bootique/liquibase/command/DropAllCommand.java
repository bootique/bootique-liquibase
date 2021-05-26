/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.liquibase.command;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.jdbc.liquibase.LiquibaseRunner;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DropAllCommand extends CommandWithMetadata {

    private static final Logger LOGGER = LoggerFactory.getLogger(DropAllCommand.class);

    private Provider<LiquibaseRunner> runnerProvider;

    @Inject
    public DropAllCommand(Provider<LiquibaseRunner> runnerProvider) {
        super(CommandMetadata
                .builder(DropAllCommand.class)
                .name("lb-drop-all")
                .description("Drops all database objects in the configured schema(s). " +
                        "Note that functions, procedures and packages are not dropped.")
                .addOptions(
                        Stream.of(
                                OptionMetadata
                                        .builder("schema")
                                        .valueRequired("schema_name")
                                        .description("Schema against which 'liquibase dropAll' will be executed.")
                                        .build(),
                                OptionMetadata
                                        .builder("catalog")
                                        .valueRequired("catalog_name")
                                        .description("Catalog against which 'liquibase dropAll' will be executed.")
                                        .build())
                                .collect(Collectors.toList()))
                .build());

        this.runnerProvider = runnerProvider;
    }


    @Override
    public CommandOutcome run(Cli cli) {
        LOGGER.info("Will run 'liquibase dropAll'...");
        return runnerProvider.get().call(lb -> run(cli, lb));
    }

    protected CommandOutcome run(Cli cli, Liquibase lb) {
        try {
            lb.dropAll(new CatalogAndSchema(cli.optionString("catalog"), cli.optionString("schema")));
            return CommandOutcome.succeeded();
        } catch (Exception e) {
            return CommandOutcome.failed(1, e);
        }
    }
}

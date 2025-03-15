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
import liquibase.Liquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class ClearCheckSumsCommand extends CommandWithMetadata {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClearCheckSumsCommand.class);

    private Provider<LiquibaseRunner> runnerProvider;

    @Inject
    public ClearCheckSumsCommand(Provider<LiquibaseRunner> runnerProvider) {
        super(CommandMetadata
                .builder(ClearCheckSumsCommand.class)
                .name("lb-clear-check-sums")
                .description("Clears all checksums in the current changelog, so they will be recalculated next update.")
                .build());
        this.runnerProvider = runnerProvider;
    }

    @Override
    public CommandOutcome run(Cli cli) {
        LOGGER.info("Will run 'liquibase clearCheckSums'...");
        return runnerProvider.get().call(this::run);
    }

    protected CommandOutcome run(Liquibase lb) {
        try {
            lb.clearCheckSums();
            return CommandOutcome.succeeded();
        } catch (Exception e) {
            return CommandOutcome.failed(1, e);
        }
    }
}


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
import io.bootique.liquibase.LiquibaseModule;
import io.bootique.liquibase.LiquibaseRunner;
import io.bootique.meta.application.CommandMetadata;
import liquibase.Contexts;
import liquibase.LabelExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;


public class UpdateCommand extends CommandWithMetadata {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCommand.class);

	private Provider<LiquibaseRunner> runnerProvider;

	@Inject
	public UpdateCommand(Provider<LiquibaseRunner> runnerProvider) {
		super(CommandMetadata.builder(UpdateCommand.class)
				.name("lb-update")
				.shortName('u')
				.description("Updates DB with available migrations")
				.build());
		this.runnerProvider = runnerProvider;
	}

	@Override
	public CommandOutcome run(Cli cli) {

		LOGGER.info("Will run 'liquibase update'...");

		return runnerProvider.get().runWithLiquibase(lb -> {
			try {
				List<String> options = cli.optionStrings(LiquibaseModule.CONTEXT_OPTION);
				lb.update(options == null ? new Contexts() : new Contexts(options.toArray(new String[options.size()])), new LabelExpression());

				return CommandOutcome.succeeded();
			} catch (Exception e) {
				return CommandOutcome.failed(1, e);
			}
		});
	}
}

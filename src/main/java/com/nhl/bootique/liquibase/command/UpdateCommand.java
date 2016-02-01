package com.nhl.bootique.liquibase.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.nhl.bootique.cli.Cli;
import com.nhl.bootique.command.CommandMetadata;
import com.nhl.bootique.command.CommandOutcome;
import com.nhl.bootique.command.CommandWithMetadata;
import com.nhl.bootique.liquibase.LiquibaseRunner;

import liquibase.Contexts;
import liquibase.LabelExpression;

public class UpdateCommand extends CommandWithMetadata {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCommand.class);

	private Provider<LiquibaseRunner> runnerProvider;

	@Inject
	public UpdateCommand(Provider<LiquibaseRunner> runnerProvider) {
		super(CommandMetadata.builder(UpdateCommand.class).description("Updates DB with available migrations").build());
		this.runnerProvider = runnerProvider;
	}

	@Override
	public CommandOutcome run(Cli cli) {

		LOGGER.info("Will run 'liquibase update'...");

		return runnerProvider.get().runWithLiquibase(lb -> {
			try {
				lb.update(new Contexts(), new LabelExpression());
				return CommandOutcome.succeeded();
			} catch (Exception e) {
				return CommandOutcome.failed(1, e);
			}
		});
	}
}

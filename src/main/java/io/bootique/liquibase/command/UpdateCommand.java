package io.bootique.liquibase.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.liquibase.LiquibaseRunner;
import liquibase.Contexts;
import liquibase.LabelExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
				lb.update(new Contexts(), new LabelExpression());
				return CommandOutcome.succeeded();
			} catch (Exception e) {
				return CommandOutcome.failed(1, e);
			}
		});
	}
}

package com.nhl.bootique.liquibase.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.nhl.bootique.command.CommandOutcome;
import com.nhl.bootique.command.OptionTriggeredCommand;
import com.nhl.bootique.jopt.Options;
import com.nhl.bootique.liquibase.LiquibaseRunner;

import joptsimple.OptionParser;
import liquibase.Contexts;
import liquibase.LabelExpression;

public class UpdateCommand extends OptionTriggeredCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCommand.class);

	private static final String UDPATE_OPTION = "update";

	private Provider<LiquibaseRunner> runnerProvider;

	@Inject
	public UpdateCommand(Provider<LiquibaseRunner> runnerProvider) {
		this.runnerProvider = runnerProvider;
	}

	@Override
	protected CommandOutcome doRun(Options options) {
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

	@Override
	protected String getOption() {
		return UDPATE_OPTION;
	}

	@Override
	public void configOptions(OptionParser parser) {
		parser.accepts(getOption(), "Updates DB with available migrations");
	}
}

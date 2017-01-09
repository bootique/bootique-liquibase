package io.bootique.liquibase.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.liquibase.LiquibaseRunner;
import io.bootique.meta.application.CommandMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 0.11
 */
public class ValidateCommand extends CommandWithMetadata {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateCommand.class);

    private Provider<LiquibaseRunner> runnerProvider;

    @Inject
    public ValidateCommand(Provider<LiquibaseRunner> runnerProvider) {
        super(CommandMetadata.builder(ValidateCommand.class)
                .description("Checks the changelog for errors.").build());
        this.runnerProvider = runnerProvider;
    }

    @Override
    public CommandOutcome run(Cli cli) {

        LOGGER.info("Will run 'liquibase validate'...");

        return runnerProvider.get().runWithLiquibase(lb -> {
            try {
                lb.validate();
                return CommandOutcome.succeeded();
            } catch (Exception e) {
                return CommandOutcome.failed(1, e);
            }
        });
    }
}

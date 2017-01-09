package io.bootique.liquibase.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.liquibase.LiquibaseRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 0.11
 */
public class ClearCheckSumsCommand extends CommandWithMetadata {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClearCheckSumsCommand.class);

    private Provider<LiquibaseRunner> runnerProvider;

    @Inject
    public ClearCheckSumsCommand(Provider<LiquibaseRunner> runnerProvider) {
        super(CommandMetadata
                .builder(ClearCheckSumsCommand.class)
                .description("Clears all checksums in the current changelog, so they will be recalculated next update.")
                .build());
        this.runnerProvider = runnerProvider;
    }

    @Override
    public CommandOutcome run(Cli cli) {

        LOGGER.info("Will run 'liquibase clearCheckSums'...");

        return runnerProvider.get().runWithLiquibase(lb -> {
            try {
                lb.clearCheckSums();
                return CommandOutcome.succeeded();
            } catch (Exception e) {
                return CommandOutcome.failed(1, e);
            }
        });
    }
}


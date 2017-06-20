package io.bootique.liquibase.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
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

/**
 * @since 0.11
 */
public class ChangelogSyncCommand extends CommandWithMetadata {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangelogSyncCommand.class);

    private Provider<LiquibaseRunner> runnerProvider;

    @Inject
    public ChangelogSyncCommand(Provider<LiquibaseRunner> runnerProvider) {
        super(CommandMetadata.builder(ChangelogSyncCommand.class)
                .description("Mark all changes as executed in the database.").build());
        this.runnerProvider = runnerProvider;
    }

    @Override
    public CommandOutcome run(Cli cli) {

        LOGGER.info("Will run 'liquibase changelogSync'...");

        return runnerProvider.get().runWithLiquibase(lb -> {
            try {
                List<String> options = cli.optionStrings(LiquibaseModule.CONTEXT_OPTION);
                lb.changeLogSync(options == null ? new Contexts() : new Contexts(options.toArray(new String[options.size()])), new LabelExpression());

                return CommandOutcome.succeeded();
            } catch (Exception e) {
                return CommandOutcome.failed(1, e);
            }
        });
    }
}

package io.bootique.liquibase.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.liquibase.LiquibaseRunner;
import io.bootique.meta.application.CommandMetadata;
import liquibase.Contexts;
import liquibase.LabelExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

/**
 * @since 0.11
 */
public class ChangelogSyncSqlCommand extends CommandWithMetadata {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangelogSyncSqlCommand.class);

    private Provider<LiquibaseRunner> runnerProvider;

    @Inject
    public ChangelogSyncSqlCommand(Provider<LiquibaseRunner> runnerProvider) {
        super(CommandMetadata.builder(ChangelogSyncSqlCommand.class)
                .description("Writes SQL to mark all changes as executed in the database to STDOUT.").build());
        this.runnerProvider = runnerProvider;
    }

    @Override
    public CommandOutcome run(Cli cli) {

        LOGGER.info("Will run 'liquibase changelogSyncSQL'...");

        return runnerProvider.get().runWithLiquibase(lb -> {
            try {
                lb.changeLogSync(new Contexts(), new LabelExpression(),
                        new BufferedWriter(new OutputStreamWriter(System.out)));
                return CommandOutcome.succeeded();
            } catch (Exception e) {
                return CommandOutcome.failed(1, e);
            }
        });
    }
}

package io.bootique.liquibase.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.liquibase.LiquibaseRunner;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import liquibase.CatalogAndSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @since 0.13
 */
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

        return runnerProvider.get().runWithLiquibase(lb -> {
            try {
                lb.dropAll(new CatalogAndSchema(cli.optionString("catalog"), cli.optionString("schema")));
                return CommandOutcome.succeeded();
            } catch (Exception e) {
                return CommandOutcome.failed(1, e);
            }
        });
    }
}

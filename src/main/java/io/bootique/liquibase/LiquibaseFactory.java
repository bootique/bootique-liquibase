package io.bootique.liquibase;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.resource.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.function.Function;

@BQConfig("Configures Liquibase migrations.")
public class LiquibaseFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseFactory.class);

    private String datasource;

    @Deprecated
    private String changeLog;

    private Collection<ResourceFactory> changeLogs;

    @BQConfigProperty("DataSource name defined under 'jdbc' that should be used for migrations execution.")
    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    /**
     * Initializes a collection of Liquibase change log files. They will be executed in the provided order.
     *
     * @param changeLogs
     * @since 0.11
     */
    @BQConfigProperty
    public void setChangeLogs(Collection<ResourceFactory> changeLogs) {
        this.changeLogs = changeLogs;
    }

    /**
     * @param changeLog
     * @deprecated since 0.11 in favor of {@link #setChangeLogs(Collection)}. Note that the new format is a
     * ResourceFactory with all associated rules (i.e. by default path is a file path, not a classpath).
     */
    @Deprecated
    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    public LiquibaseRunner createRunner(DataSourceFactory dataSourceFactory,
                                        Function<Collection<ResourceFactory>, Collection<ResourceFactory>> changeLogMerger) {
        DataSource ds = getDataSource(dataSourceFactory);

        if (changeLog != null) {

            if (changeLogs != null) {
                throw new IllegalStateException("Using both old style 'changeLog' property and new style 'changeLogs'. " +
                        "You can use either one or the other");
            }

            String asClasspath = "classpath:" + changeLog;
            LOGGER.warn("Using deprecated 'changeLog' property. " +
                    "Consider switching to 'changeLogs' collection instead. " +
                    "The new value will be '" + asClasspath + "'");

            return new LegacyLiquibaseRunner(changeLog, ds);
        }


        Collection<ResourceFactory> allChangeLogs = changeLogMerger.apply(changeLogs);
        return new LiquibaseRunner(allChangeLogs, ds);
    }

    private DataSource getDataSource(DataSourceFactory dataSourceFactory) {
        DataSource ds;
        Collection<String> allNames = dataSourceFactory.allNames();

        if (datasource == null) {
            if (allNames.isEmpty()) {
                throw new IllegalStateException("No DataSources are available for Liquibase. " +
                        "Add a DataSource via 'bootique-jdbc'or 'bootique-liquibase'");
            }

            if (allNames.size() == 1) {
                return dataSourceFactory.forName(allNames.iterator().next());
            } else {
                throw new IllegalStateException(
                        String.format("Can't map Liquibase DataSource: 'liquibase.datasource' is missing. " +
                                "Available DataSources are %s", allNames));
            }
        } else {
            if (!allNames.contains(datasource)) {
                throw new IllegalStateException(
                        String.format("Can't map Liquibase DataSource: 'liquibase.datasource' is set to '%s'. " +
                                "Available DataSources: %s", datasource, allNames));
            }

            return dataSourceFactory.forName(datasource);
        }
    }
}

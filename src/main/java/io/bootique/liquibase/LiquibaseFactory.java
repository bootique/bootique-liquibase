/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.liquibase;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.cli.Cli;
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

    public LiquibaseRunner createRunner(DataSourceFactory dataSourceFactory,
                                        Function<Collection<ResourceFactory>,
                                                Collection<ResourceFactory>> changeLogMerger,
                                        Cli cli) {
        DataSource ds = getDataSource(dataSourceFactory);
        Collection<ResourceFactory> allChangeLogs = changeLogMerger.apply(changeLogs);
        return new LiquibaseRunner(allChangeLogs, ds, cli);
    }

    private DataSource getDataSource(DataSourceFactory dataSourceFactory) {
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

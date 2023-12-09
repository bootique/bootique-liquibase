/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.liquibase;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.cli.Cli;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.liquibase.LiquibaseRunner;
import io.bootique.liquibase.annotation.ChangeLogs;
import io.bootique.resource.ResourceFactory;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.Set;

@BQConfig("Configures Liquibase migrations.")
public class LiquibaseRunnerFactory {

    private final DataSourceFactory dataSourceFactory;
    private final ChangeLogMerger changeLogMerger;
    @ChangeLogs
    private final Set<ResourceFactory> injectedChangeLogs;
    private final Cli cli;

    private String datasource;
    private Collection<ResourceFactory> changeLogs;

    @Inject
    public LiquibaseRunnerFactory(
            DataSourceFactory dataSourceFactory,
            ChangeLogMerger changeLogMerger,
            @ChangeLogs Set<ResourceFactory> injectedChangeLogs,
            Cli cli) {
        this.dataSourceFactory = dataSourceFactory;
        this.changeLogMerger = changeLogMerger;
        this.injectedChangeLogs = injectedChangeLogs;
        this.cli = cli;
    }

    @BQConfigProperty("DataSource name defined under 'jdbc' that should be used for migrations execution.")
    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    @BQConfigProperty("A collection of Liquibase change log files executed in the provided order.")
    public void setChangeLogs(Collection<ResourceFactory> changeLogs) {
        this.changeLogs = changeLogs;
    }

    public LiquibaseRunner create() {
        DataSource ds = findDataSource();
        return new LiquibaseRunner(
                changeLogMerger.merge(injectedChangeLogs, changeLogs),
                ds,
                cli.optionString(LiquibaseModule.DEFAULT_SCHEMA_OPTION));
    }

    private DataSource findDataSource() {
        return datasource != null
                ? namedDataSource(datasource)
                : defaultDataSource();
    }

    private DataSource defaultDataSource() {
        Collection<String> allNames = dataSourceFactory.allNames();

        if (allNames.isEmpty()) {
            throw new IllegalStateException("No DataSources configured. You may configure a DataSource via 'bootique-jdbc'");
        }

        if (allNames.size() == 1) {
            return dataSourceFactory.forName(allNames.iterator().next());
        }

        throw new IllegalStateException(
                String.format("Multiple DataSources are available (%s). You must specify the name of Liquibase " +
                        "DataSource explicitly via 'liquibase.datasource'", allNames));
    }

    private DataSource namedDataSource(String name) {
        return dataSourceFactory.forName(name);
    }
}

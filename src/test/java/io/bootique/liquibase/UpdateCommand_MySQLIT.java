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

import io.bootique.command.CommandOutcome;
import io.bootique.jdbc.junit5.Table;
import io.bootique.jdbc.junit5.tc.TcDbTester;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@BQTest
public class UpdateCommand_MySQLIT {

    @BQTestTool
    final static TcDbTester db = TcDbTester.db("jdbc:tc:mysql:8.0.20:///");

    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void migration() {
        CommandOutcome result = testFactory
                .app("--config", "classpath:io/bootique/liquibase/migration_mySQL.yml", "-u")
                .autoLoadModules()
                .module(db.moduleWithTestDataSource("ds"))
                .run();

        assertTrue(result.isSuccess());

        Table x = db.getTable("X");
        x.matcher().assertOneMatch();
        x.matcher().eq("ID", 1).andEq("SCHEMA", "testX").assertOneMatch();

        Table y = db.getTable("Y");
        y.matcher().assertOneMatch();
        y.matcher().eq("ID", 1).andEq("SCHEMA", "testY").assertOneMatch();
    }
}

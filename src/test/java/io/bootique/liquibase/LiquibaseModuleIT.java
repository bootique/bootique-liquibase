package io.bootique.liquibase;

import io.bootique.command.CommandOutcome;
import io.bootique.test.BQTestRuntime;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class LiquibaseModuleIT {

    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testMigration() {

        BQTestRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/liquibase/migrations1.yml", "-u")
                .autoLoadModules()
                .createRuntime();

        CommandOutcome result = runtime.run();
        Assert.assertTrue(result.isSuccess());

//        Table a = DatabaseChannel.get(runtime).newTable("A").columnNames("ID", "NAME").build();
//        Object[] row = a.selectOne();
//        assertEquals(1, row[0]);
//        assertEquals("AA", row[1]);
    }
}

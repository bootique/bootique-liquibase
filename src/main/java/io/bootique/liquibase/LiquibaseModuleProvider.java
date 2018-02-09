package io.bootique.liquibase;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.jdbc.JdbcModuleProvider;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class LiquibaseModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new LiquibaseModule();
    }

    @Override
    public Map<String, Type> configs() {
        // TODO: config prefix is hardcoded. Refactor away from ConfigModule, and make provider
        // generate config prefix, reusing it in metadata...
        return Collections.singletonMap("liquibase", LiquibaseFactory.class);
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Provides integration with Liquibase SQL migrations framework.");
    }

    @Override
    public Collection<BQModuleProvider> dependencies() {
        return Collections.singletonList(
                new JdbcModuleProvider()
        );
    }
}

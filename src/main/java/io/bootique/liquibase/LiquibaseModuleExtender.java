package io.bootique.liquibase;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import io.bootique.ModuleExtender;
import io.bootique.liquibase.annotation.ChangeLogs;
import io.bootique.resource.ResourceFactory;

/**
 * @since 0.12
 */
public class LiquibaseModuleExtender extends ModuleExtender<LiquibaseModuleExtender> {

    private Multibinder<ResourceFactory> changeLogs;

    public LiquibaseModuleExtender(Binder binder) {
        super(binder);
    }

    @Override
    public LiquibaseModuleExtender initAllExtensions() {
        contributeChangeLogs();
        return this;
    }

    public LiquibaseModuleExtender addChangeLog(ResourceFactory changeLog) {
        contributeChangeLogs().addBinding().toInstance(changeLog);
        return this;
    }

    public LiquibaseModuleExtender addChangeLog(String changeLog) {
        return addChangeLog(new ResourceFactory(changeLog));
    }

    protected Multibinder<ResourceFactory> contributeChangeLogs() {
        return changeLogs != null ? changeLogs : (changeLogs = newSet(ResourceFactory.class, ChangeLogs.class));
    }
}

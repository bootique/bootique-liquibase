package io.bootique.liquibase;

import io.bootique.resource.ResourceFactory;

import java.util.Collection;
import java.util.Objects;

/**
 * A simple change log merger that uses "last wins" strategy, returning the last change log collection passed to the
 * method.
 *
 * @since 0.11
 */
public class ChangeLogMerger {

    public Collection<ResourceFactory> merge(Collection<ResourceFactory> logs1,
                                             Collection<ResourceFactory> logs2) {
        return logs2 != null ? logs2 : Objects.requireNonNull(logs1);
    }
}

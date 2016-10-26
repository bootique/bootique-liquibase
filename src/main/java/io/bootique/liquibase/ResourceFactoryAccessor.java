package io.bootique.liquibase;

import io.bootique.resource.ResourceFactory;
import liquibase.resource.ResourceAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Set;


/**
 * A Liquibase {@link ResourceAccessor} that treats paths as Bootique resource URLs that should be processed via
 * {@link ResourceFactory}.
 *
 * @since 0.11
 */
public class ResourceFactoryAccessor implements ResourceAccessor {

    @Override
    public Set<InputStream> getResourcesAsStream(String path) throws IOException {
        URL url = new ResourceFactory(path).getUrl();
        return Collections.singleton(url.openStream());
    }

    @Override
    public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        throw new UnsupportedOperationException("'list' operation is not defined");
    }

    @Override
    public ClassLoader toClassLoader() {
        return getClass().getClassLoader();
    }
}

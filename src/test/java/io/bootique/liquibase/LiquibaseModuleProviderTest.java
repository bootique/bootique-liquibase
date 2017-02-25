package io.bootique.liquibase;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class LiquibaseModuleProviderTest {

	@Test
	public void testPresentInJar() {
		BQModuleProviderChecker.testPresentInJar(LiquibaseModuleProvider.class);
	}

	@Test
	public void testMetadata() {
		BQModuleProviderChecker.testMetadata(LiquibaseModuleProvider.class);
	}
}

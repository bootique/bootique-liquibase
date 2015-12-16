package com.nhl.bootique.liquibase;

import static org.junit.Assert.assertTrue;

import java.util.ServiceLoader;

import org.junit.Test;

import com.nhl.bootique.BQModuleProvider;

public class LiquibaseModuleProviderIT {

	@Test
	public void testPresentInJar() {

		boolean[] found = { false };

		ServiceLoader.load(BQModuleProvider.class).forEach(p -> {
			if (p instanceof LiquibaseModuleProvider) {
				found[0] = true;
			}
		});

		assertTrue(found[0]);
	}
}

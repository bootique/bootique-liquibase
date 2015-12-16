package com.nhl.bootique.liquibase;

import com.google.inject.Module;
import com.nhl.bootique.BQModuleProvider;

public class LiquibaseModuleProvider implements BQModuleProvider {

	@Override
	public Module module() {
		return new LiquibaseModule();
	}
}

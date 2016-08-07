package io.bootique.liquibase;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;

public class LiquibaseModuleProvider implements BQModuleProvider {

	@Override
	public Module module() {
		return new LiquibaseModule();
	}
}

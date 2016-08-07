package io.bootique.liquibase;

import liquibase.logging.core.AbstractLogger;

public class SLFLiquibaseAdapter extends AbstractLogger {

	private org.slf4j.Logger slfLogger;

	public SLFLiquibaseAdapter(org.slf4j.Logger slfLogger) {
		this.slfLogger = slfLogger;
	}

	@Override
	public int getPriority() {
		return 1;
	}

	@Override
	public void setName(String name) {
		// do nothing
	}

	@Override
	public void setLogLevel(String logLevel, String logFile) {
		// ignoring file...
		setLogLevel(logLevel);
	}

	@Override
	public void severe(String message) {
		slfLogger.error(buildMessage(message));
	}

	@Override
	public void severe(String message, Throwable e) {
		slfLogger.error(buildMessage(message), e);
	}

	@Override
	public void warning(String message) {
		slfLogger.warn(buildMessage(message));
	}

	@Override
	public void warning(String message, Throwable e) {
		slfLogger.warn(buildMessage(message), e);
	}

	@Override
	public void info(String message) {
		slfLogger.info(buildMessage(message));
	}

	@Override
	public void info(String message, Throwable e) {
		slfLogger.info(buildMessage(message), e);
	}

	@Override
	public void debug(String message) {
		slfLogger.debug(buildMessage(message));
	}

	@Override
	public void debug(String message, Throwable e) {
		slfLogger.debug(buildMessage(message), e);
	}
}

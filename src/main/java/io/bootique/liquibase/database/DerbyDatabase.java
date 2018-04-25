package io.bootique.liquibase.database;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogService;

import java.lang.reflect.Field;

/**
 * Reimplements "close" method for Liquibase {@link liquibase.database.core.DerbyDatabase} to prevent Derby shutdown.
 * Liquibase operates with Bootique-provided DataSource and should not attempt to manage underlying DB state.
 *
 * @since 0.11
 */
public class DerbyDatabase extends liquibase.database.core.DerbyDatabase {

    @Override
    public int getPriority() {
        // higher priority allows us to override default Derby DB object
        return super.getPriority() + 1;
    }

    @Override
    public void close() throws DatabaseException {

        // copy-paste from AbstractJdbcDatabase plus reflection to deal with private fields...

        ExecutorService.getInstance().clearExecutor(this);
        DatabaseConnection connection = getConnection();
        if (connection != null) {

            Boolean previousAutoCommit = previousAutoCommit();
            if (previousAutoCommit != null) {
                try {
                    connection.setAutoCommit(previousAutoCommit);
                } catch (DatabaseException e) {
                    LogService.getLog(getClass()).warning("Failed to restore the auto commit to " + previousAutoCommit);
                    throw e;
                }
            }
            connection.close();
        }
    }

    private Boolean previousAutoCommit() throws DatabaseException {

        // ugly hack ... reading private field from superclass...

        Field fPreviousAutoCommit = null;
        try {
            fPreviousAutoCommit = AbstractJdbcDatabase.class.getDeclaredField("previousAutoCommit");
        } catch (NoSuchFieldException e) {
            throw new DatabaseException("Error reading auto-commit data", e);
        }

        fPreviousAutoCommit.setAccessible(true);
        try {
            return (Boolean) fPreviousAutoCommit.get(this);
        } catch (IllegalAccessException e) {
            throw new DatabaseException("Error reading auto-commit data", e);
        }
    }
}

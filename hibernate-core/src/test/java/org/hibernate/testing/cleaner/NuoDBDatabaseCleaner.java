package org.hibernate.testing.cleaner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.HibernateException;
import org.hibernate.testing.schema.CheckClearSchemaListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nuodb.jdbc.RemConnection;

/**
 * <b>NUODB OVERRIDE CLASS</b>
 * <p>
 * The cleaner class for NuoDB. A test listener, registered by Hibernate, runs
 * before any tests and creates a {@link DatabaseCleaner} to use.
 * <p>
 * For further details, see {@link CheckClearSchemaListener} and
 * {@link org.junit.platform.launcher.core.DefaultLauncherSession} (relevant
 * code is in its constructor).
 * 
 * @author Paul Chapman
 */
class NuoDBDatabaseCleaner implements DatabaseCleaner {

	private static final String CURRENT_MARKER = " (current)";
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void addIgnoredTable(String tableName) {
	}

	@Override
	public boolean isApplicable(Connection connection) {
		return (connection instanceof RemConnection);
	}

	@Override
	public void clearAllSchemas(Connection connection) {

		try {
			String[] schemas = new String[0];

			// Fetch a list of all schemas.
			try (ResultSet results = connection //
					.createStatement().executeQuery("show schemas")) {
				if (results.next()) {
					schemas = results.getString(1).trim().split(System.lineSeparator());
				}
			}

			// Delete them all. Ignore any blank lines, the line 'Found N schemas" and don't
			// ty to delete the SYSTEM schema.
			for (String schemaName : schemas) {
				schemaName = schemaName.trim();

				if (schemaName.length() == 0 || schemaName.startsWith("Found ") || schemaName.equals("SYSTEM"))
					continue;

				if (schemaName.endsWith(CURRENT_MARKER))
					// Current schema has " (current)" after its name - remove it.
					schemaName = schemaName.substring(0, schemaName.length() - CURRENT_MARKER.length());
				else if (schemaName.indexOf('-') != -1)
					// Illegal character, wrap in double quotes
					schemaName = '"' + schemaName + '"';

				logger.info("Deleting schema '{}'", schemaName);
				clearSchema(connection, schemaName);
			}

			// There should only be one schema left - SYSTEM
			try (ResultSet results = connection //
					.createStatement().executeQuery("show schemas")) {
				if (results.next()) {
					String schemaList = results.getString(1).trim().replace('\n', ' ').replace('\r', ' ');

					if (!schemaList.contains("Found 1 schemas"))
						logger.error("After deleting schemas, {}", schemaList);
				}
			}
		} catch (SQLException e) {
			logger.error("{}: Failed running 'show schemas'", e.getClass().getName(), e.getLocalizedMessage());
		}

	}

	@Override
	public void clearSchema(Connection connection, String schemaName) {
		logger.info("clearSchema: Dropping schema " + schemaName);

		try (Statement stmt = connection.createStatement()) {
			stmt.execute("DROP SCHEMA " + schemaName + " CASCADE IF EXISTS");
		} catch (SQLException e) {
			logger.error("Unable to delete schema '{}': {}", schemaName, e.getLocalizedMessage());
			throw new HibernateException(e);
		}

	}

	@Override
	public void clearAllData(Connection connection) {

	}

	@Override
	public void clearData(Connection connection, String schemaName) {

	}
}
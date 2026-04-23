package org.hibernate.testing.cleaner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.dialect.Dialect;
import java.util.logging.Logger;

import com.nuodb.hibernate.NuoDBDialect;

/**
 * A cleaner for NuoDB. Implements {@code clearSchema} by dropping the schema
 * and recreating it. Implements {@code clearData} by truncating all the tables
 * in the specified schema and dropping any sequences.
 */
class NuoDBDatabaseCleaner implements DatabaseCleaner {

	// Class Logger
	public static final Logger LOGGER = Logger.getLogger(NuoDBDatabaseCleaner.class.getName());

	private static final String NL = System.lineSeparator();

	// private final List<String> ignoredTables = new ArrayList<>();

	public NuoDBDatabaseCleaner() {
	}

	@Override
	public void addIgnoredTable(String tableName) {
		// ignoredTables.add(tableName);
		throw new UnsupportedOperationException("addIgnoredTable not implemented for NuoDB");
	}

	/**
	 * Does this cleaner apply to the underlying database?
	 * 
	 * @param connection Database connection.
	 * @return Applies or not?
	 */
	@Override
	public boolean isApplicable(Connection connection) {
		try {
			return connection.getMetaData().getDatabaseProductName().startsWith("NuoDB");
		} catch (SQLException e) {
			throw new RuntimeException("Could not resolve the database metadata", e);
		}
	}

	/**
	 * Clear all the schemas in the database (except the SYSTEM schema).
	 * 
	 * @param connection Database connection.
	 */
	@Override
	public void clearAllSchemas(Connection connection) {

		try (Statement stmt = connection.createStatement()) {
			Set<String> schemas = getAllSchemas(stmt);

			for (String schemaName : schemas)
				clearSchema(connection, schemaName);

			LOGGER.warning(">>>> " + schemas.size() + " schemas cleared (dropped and recreated) successfully.");
		} catch (SQLException e) {
			logError("Failed clearing all schemas:", e);
		}
	}

	/**
	 * Empty the specified schema of all data.
	 * 
	 * @param connection Database connection.
	 * @param schemaName Name of the schema to clear.
	 */
	@Override
	public void clearSchema(Connection connection, String schemaName) {
		System.out.println(">>>> Clear schema " + schemaName);
		LOGGER.warning("Clear schema " + schemaName);

		try (Statement stmt = connection.createStatement()) {
			// Drop the entire schema and its contents
			stmt.execute("DROP SCHEMA " + schemaName + " CASCADE");

			// Recreate the schema
			stmt.execute("CREATE SCHEMA " + schemaName);

			// Sanity check - is it empty?
			stmt.execute("USE " + schemaName);
			ResultSet rs = stmt.executeQuery("SHOW TABLES");

			while (rs.next()) {
				String result = rs.getString(1);

				if (!result.contains("No tables found"))
					System.out.println(">>>> " + schemaName + " NOT EMPTY ----> " + rs.getString(1));
			}
		} catch (SQLException e) {
			System.err.println(" ---> Failed clearing schema " + schemaName + ":" + e.getLocalizedMessage());
			logError("Failed clearing schema " + schemaName, e);
		}
	}

	/**
	 * Clear all data in all schemas in the database (except the SYSTEM schema).
	 * 
	 * @param connection Database connection.
	 */
	@Override
	public void clearAllData(Connection connection) {
		try (Statement stmt = connection.createStatement()) {
			Set<String> schemas = getAllSchemas(stmt);

			for (String schemaName : schemas)
				clearData(connection, schemaName);

			System.out.println(">>>> " + schemas.size() + " schemas had their tables truncated successfully.");
		} catch (SQLException e) {
			logError("Failed clearing all data:", e);
		}
	}

	/**
	 * Clear all data in the specified schema - specifically empty all tables and .
	 * 
	 * @param connection Database connection.
	 * @param schemaName Name of the schema to clear.
	 */
	@Override
	public void clearData(Connection connection, String schemaName) {

		try (Statement stmt = connection.createStatement()) {
			truncateTables(schemaName, stmt);
		} catch (SQLException e) {
			logError("Failed clearing all data in schema '" + schemaName, e);
			e.printStackTrace();
		}
	}

	/**
	 * Get all the schemas (except the SYSTEM Schema).
	 * 
	 * @param stmt A JDBC Statement instance.
	 * @return The schemas found.
	 * @throws SQLException
	 */
	protected Set<String> getAllSchemas(Statement stmt) throws SQLException {
		ResultSet rs = stmt.executeQuery("SELECT schema FROM SYSTEM.Schemas");
		Set<String> schemas = new TreeSet<>();

		while (rs.next()) {
			String schemaName = rs.getString(1);

			if (!schemaName.equals("SYSTEM"))
				schemas.add(schemaName);
		}

		return schemas;
	}

	/**
	 * Truncate all the tables in the specified schema
	 * 
	 * @param schemaName The schema to use.
	 * @param stmt       A JDBC Statement instance.
	 *
	 * @throws SQLException Any failure.
	 */
	protected void truncateTables(String schemaName, Statement stmt) throws SQLException {
		System.out.println(">>>> Truncating tables in schema " + schemaName);
		LOGGER.warning("Truncating tables in schema " + schemaName);

		// First find all the tables
		ResultSet rs = stmt.executeQuery("SELECT tablename FROM SYSTEM.Tables WHERE schema = '" + schemaName + '\'');
		Set<String> tables = new TreeSet<>();

		while (rs.next()) {
			String tableName = rs.getString(1);
			tables.add(tableName);
		}

		// Truncate all the tables found by using a single call to the database
		StringBuilder sb = new StringBuilder();

		for (String tableName : tables) {
			sb.append("TRUNCATE TABLE ").append(schemaName).append('.').append(tableName).append(';').append(NL);
		}

		String sql = sb.toString();

		if (sql.length() > 0) {
			LOGGER.info("SQL to truncate all tables in schema '" + schemaName + "' is " + NL + sql);
			stmt.execute(sql);
		} else
			LOGGER.info("No tables to truncate in schema '" + schemaName + '\'');
	}

	private void logError(String emsg, Exception e) {
		LOGGER.severe(emsg + ':' + e.getLocalizedMessage() + " (" + e.getClass().getSimpleName() + ')');
	}
}
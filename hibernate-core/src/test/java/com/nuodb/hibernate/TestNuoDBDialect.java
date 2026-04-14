package com.nuodb.hibernate;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import org.hibernate.Version;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;

import com.nuodb.jdbc.DataSource;

/**
 * Test dialect that always knows the underlying NuoDB database version.
 * The Hibernate tests expect to use the default constructor to create a
 * dialect instance, bypassing dialect resolution.  This class uses the
 * same mechanism as dialect resolution (JDBC meta-data) and sets the
 * database version information for itself.
 * <p>
 * This class uses no additional APIs, but reads {@code hibernate.properties}
 * from the root of class path (the file in {@code src/test/resources}) to find
 * connection details.
 * 
 * @author Paul Chapman
 */
public class TestNuoDBDialect extends NuoDBDialect {
	/**
	 * Internal JDK logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(TestNuoDBDialect.class.getName());

	private static final String JPA_PROP_PREFIX = "javax.persistence.jdbc.";

	private static NuoDBDialectResolutionInfo dbInfo = null;

	private SQLExceptionConversionDelegate nuoDbSQLExceptionConversionDelegate;

	/**
	 * Get the current database product name and version from the JDBC meta datak.
	 * 
	 * @return The database name and version information.
	 */
	private static DialectResolutionInfo getDatabaseVersion() {

		if (dbInfo == null) {
			// Load the Hibernate properties
			InputStream input = TestNuoDBDialect.class.getClassLoader().getResourceAsStream("hibernate.properties");
			Properties dbProps = new Properties();

			try {
				dbProps.load(input);

				// Copy the hibernate database properties to the equivalent properties for NuoDB's data source
				dbProps.setProperty(DataSource.PROP_URL, dbProps.getProperty(JPA_PROP_PREFIX + "url"));
				dbProps.setProperty(DataSource.PROP_USER, dbProps.getProperty(JPA_PROP_PREFIX + "user"));
				dbProps.setProperty(DataSource.PROP_PASSWORD, dbProps.getProperty(JPA_PROP_PREFIX + "password"));

				String schema = dbProps.getProperty("hibernate.default_schema");
				dbProps.setProperty(DataSource.PROP_SCHEMA, schema == null ? "User" : schema);
				dbProps.setProperty("jdk14Logger", "true");
				dbProps.setProperty(DataSource.PROP_MAXAGE, "1000");
				dbProps.setProperty(DataSource.PROP_MAXACTIVE, "1");
				dbProps.setProperty(DataSource.PROP_INITIALSIZE, "1");

			} catch (IOException e) {
				throw new RuntimeException("Unable to load classpath:hibernate.properties", e);
			}

			LOGGER.fine("Database properties: " + dbProps);
			try (DataSource dataSource = new DataSource(dbProps)) {

				// Extract database product name and version from the JDBC meta-data
				try (Connection conn = dataSource.getConnection()) {
					LOGGER.info("Txn Isolation = " + conn.getTransactionIsolation());

					DatabaseMetaData metaData = conn.getMetaData();
					String databaseName = metaData.getDatabaseProductName();
					int major = metaData.getDatabaseMajorVersion();
					int minor = metaData.getDatabaseMinorVersion();
					int patch = 0;

					// The patch (micro) version number is only in the full product string
					String vstr = metaData.getDatabaseProductVersion();
					int ix1 = vstr.lastIndexOf(".");
					int ix2 = vstr.lastIndexOf("-");
					String patchStr = ix2 == -1 ? vstr.substring(ix1 + 1) : vstr.substring(ix1 + 1, ix2);

					LOGGER.info("NuoDB Database version: " + vstr);
					LOGGER.info("NuoDB Database patch version: " + patchStr);

					try {
						// This should be a string of one or more digits
						patch = Integer.parseInt(patchStr);
					} catch (NumberFormatException e) {
						LOGGER.severe("Unable to access patch version from '" + vstr + "', defaulting to 0");
					}

					LOGGER.info("Found Hibernate V" + Version.getVersionString());
					final String hibernateVersion = Version.class.getPackage().getImplementationVersion();
					LOGGER.info("Found Hibernate V" + hibernateVersion);

					dbInfo = new NuoDBDialectResolutionInfo(databaseName, major, minor, patch);
				} catch (SQLException e) {
					throw new RuntimeException("Failure accessing database meta-data: " + e.getLocalizedMessage(), e);
				} 
			} catch (IOException e) {
				LOGGER.severe("Data source error: " + e.getLocalizedMessage());
			}
		}

		// Should never happen
		if (dbInfo == null)
			throw new IllegalStateException("Unable to setup NuoDBDialectResolutionInfo");

		return dbInfo;
	}

	/** Mini test program. */
	public static void main(String[] args) {
		new TestNuoDBDialect();
	}

	/**
	 * No-arg constructor. Many {@code hibernate_orm} tests still load by creating
	 * an instance from the {@code hibernate.dialect} property. So a default
	 * constructor is needed. Fetches database version from JDBC meta-data, the same
	 * way the dialect resolver does it.
	 */
	public TestNuoDBDialect() {
		this(getDatabaseVersion());

		LOGGER.info("    NOTE: Running TestNuoDBDialect. Database version is " + getVersion());
		nuoDbSQLExceptionConversionDelegate = super.buildSQLExceptionConversionDelegate();
	}

	/**
	 * Create an instance for the specified database version.
	 * 
	 * @param info Details of the database and its version.
	 */
	public TestNuoDBDialect(DialectResolutionInfo info) {
		super(info);
		LOGGER.info("    NOTE: Running TestNuoDBDialect. Database version is " + getVersion());
		nuoDbSQLExceptionConversionDelegate = super.buildSQLExceptionConversionDelegate();
	}

	/**
	 * Make insertHint visible for testing.
	 */
	@Override
	public String insertHint(String sql, String hint) {
		return super.insertHint(sql, hint);
	}

}
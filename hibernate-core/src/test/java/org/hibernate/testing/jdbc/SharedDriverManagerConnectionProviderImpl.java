/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.testing.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.internal.ConnectionProviderInitiator;
import org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl;
import org.hibernate.internal.util.config.ConfigurationHelper;

/**
 * A special connection provider that is shared across test runs for better performance.
 *
 * @author Christian Beikov
 */
public class SharedDriverManagerConnectionProviderImpl extends DriverManagerConnectionProviderImpl {

	private static final SharedDriverManagerConnectionProviderImpl INSTANCE = new SharedDriverManagerConnectionProviderImpl();

	public static SharedDriverManagerConnectionProviderImpl getInstance() {
		return INSTANCE;
	}

	private Config config;
	private Boolean supportsIsValid;

	@Override
	public void configure(Map configurationValues) {
		final Config c = new Config( configurationValues );
		if ( !c.isCompatible( config ) ) {
			if ( config != null ) {
				super.stop();
			}
			super.configure( configurationValues );
			config = c;
		}
	}

	@Override
	public boolean isValid(Connection connection) throws SQLException {
		if ( supportsIsValid == Boolean.FALSE  ) {
			// Assume is valid if the driver doesn't support the check
			return true;
		}
		Boolean supportsIsValid = Boolean.FALSE;
		try {
			// Wait at most 5 seconds to validate a connection is still valid
			boolean valid = connection.isValid( 5 );
			supportsIsValid = Boolean.TRUE;
			return valid;
		}
		catch (AbstractMethodError e) {
			return true;
		}
		finally {
			this.supportsIsValid = supportsIsValid;
		}
	}

	@Override
	public void stop() {
		// No need to stop as this is a shared instance
		validateConnectionsReturned();
	}

	public void reset() {
		super.stop();
	}

	private static class Config {
		private final boolean autoCommit;
		private final int minSize;
		private final int maxSize;
		private final int initialSize;
		private final String driverClassName;
		private final String url;
		private final Properties connectionProps;
		private final Integer isolation;
		private final String url2; // NUODB: Added for null URL check

		public Config(Map<String,Object> configurationValues) {
			// NUODB Start: Check driver and url values
			System.err.println("Config ctr, driver=" + configurationValues.get( AvailableSettings.DRIVER ));
			System.err.println("Config ctr,    url=" + configurationValues.get( AvailableSettings.URL ));
			System.err.println("Config ctr,   url2=" + (url2 = (String)configurationValues.get( AvailableSettings.URL + '2' )));
			// NUODB: End

			this.autoCommit = ConfigurationHelper.getBoolean( AvailableSettings.AUTOCOMMIT, configurationValues, false );
			this.minSize = ConfigurationHelper.getInt( MIN_SIZE, configurationValues, 2 );
			this.maxSize = ConfigurationHelper.getInt( AvailableSettings.POOL_SIZE, configurationValues, 20 );
			this.initialSize = ConfigurationHelper.getInt( INITIAL_SIZE, configurationValues, minSize );
			this.driverClassName = (String) configurationValues.get( AvailableSettings.DRIVER );

			// NUODB Start:
			// this.url = (String) configurationValues.get( AvailableSettings.URL);  // Original code
			String temp = (String) configurationValues.get( AvailableSettings.URL);
			this.url = temp == null ? url2 : temp;
			// NUODB: End

			this.connectionProps = ConnectionProviderInitiator.getConnectionProperties( configurationValues );
			this.isolation = ConnectionProviderInitiator.extractIsolation( configurationValues );
		}

		boolean isCompatible(Config config) {
			// NUODB Start: Added try/catch block to get info on NullPointerException
			try {
			// NUODB: End
				return config != null && autoCommit == config.autoCommit && minSize == config.minSize
						&& maxSize == config.maxSize && initialSize == config.initialSize
						&& driverClassName.equals( config.driverClassName )
						&& url.equals( config.url )
						&& connectionProps.equals( config.connectionProps )
						&& Objects.equals( isolation, config.isolation );
			// NUODB: Start
			} catch (NullPointerException e) {
				System.err.println("Config isCompatible null ptr exception");
				System.err.println("    driver=" + driverClassName + " " + config.driverClassName);
				System.err.println("      url=" + url + " " + config.url);
				System.err.println("     url2=" + url2);
				throw e;
			}
			// NUODB: End
		}

	}
}

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.testing.cleaner;

import java.util.logging.Logger;

/**
 * NUODB OVERRIDE CLASS
 * <p>
 * Added a DatabaseCleaner for NuoDB.
 * 
 * @author Christian Beikov
 */
public final class DatabaseCleanerContext {

	public static final DatabaseCleaner CLEANER;

	private static final Logger LOGGER = Logger.getLogger(DatabaseCleanerContext.class.getName());

	static {
		System.out.println(">>>> Using NuoDB's modified DatabaseCleanerContext");
		LOGGER.warning("Using NuoDB's modified DatabaseCleanerContext");

		CLEANER = JdbcConnectionContext.workReturning(connection -> {
			final DatabaseCleaner[] cleaners = new DatabaseCleaner[] { //
					new DB2DatabaseCleaner(), //
					new H2DatabaseCleaner(), //
					new SQLServerDatabaseCleaner(), //
					new MySQL5DatabaseCleaner(), //
					new MySQL8DatabaseCleaner(), //
					new MariaDBDatabaseCleaner(), //
					new OracleDatabaseCleaner(), //
					new NuoDBDatabaseCleaner(), // Added NUODB Cleaner
					new PostgreSQLDatabaseCleaner() //
			};

			for (DatabaseCleaner cleaner : cleaners) {
				if (cleaner.isApplicable(connection)) {
					System.out.println(">>>> CLEANER is " + cleaner.getClass());
					LOGGER.info("CLEANER is " + cleaner.getClass());
					return cleaner;
				}
			}

			System.err.println(" ---> No suitable cleaner found - defaulting to NuoDB");
			LOGGER.severe("No suitable cleaner found - defaulting to NuoDB");
			return new NuoDBDatabaseCleaner(); /// null;
		});
	}

	private DatabaseCleanerContext() {
	}
}

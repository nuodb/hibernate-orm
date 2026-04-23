/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.testing.junit4;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.transaction.SystemException;

import org.hibernate.engine.transaction.internal.jta.JtaStatusHelper;

import org.hibernate.testing.AfterClassOnce;
import org.hibernate.testing.cleaner.DatabaseCleaner;
import org.hibernate.testing.jdbc.leak.ConnectionLeakUtil;
import org.hibernate.testing.jta.TestingJtaPlatformImpl;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import org.jboss.logging.Logger;

/**
 * NUODB OVERRIDE CLASS
 * <p>
 * Reduced timeout to 3 mins - otherwise failed tests take too long.
 * <p>
 * The base unit test adapter.
 *
 * @author Steve Ebersole
 */
@RunWith( CustomRunner.class )
public abstract class BaseUnitTestCase {

	// static {
	// 	// NuoDB
	// 	//System.out.println(">>>> NUODB BaseUnitTestCase: Clear schemas 1 ...");
	// 	//DatabaseCleaner.clearSchemas();
	// }

	protected final Logger log = Logger.getLogger( getClass() );

	private static boolean enableConnectionLeakDetection = Boolean.TRUE.toString()
			.equals( System.getenv( "HIBERNATE_CONNECTION_LEAK_DETECTION" ) );

	private ConnectionLeakUtil connectionLeakUtil;

	protected final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // NuoDB BEGIN: change timeout from 30 mins to 3 minutes
	@Rule
	public TestRule globalTimeout = Timeout.millis( TimeUnit.MINUTES.toMillis( 3 ) ); // no test should run longer than 3 minutes
    // NUODB: END

	public BaseUnitTestCase() {
		System.out.println(">>>> Running NuoDB's modifeid BaseUnitTestCase");

		if ( enableConnectionLeakDetection ) {
			connectionLeakUtil = new ConnectionLeakUtil();
		}

		System.out.println(">>>> NUODB BaseUnitTestCase: Clear schemas 2 ...");
		DatabaseCleaner.clearSchemas();

	}

	@AfterClassOnce
	public void assertNoLeaks() {
		// NuoDB BEGIN: Clean up after tests.  Not sure why this is necessary with NuoDB 7.0.3.
		System.out.println(">>>> Clear schemas after tests");

		try {
			DatabaseCleaner.clearSchemas();
		} catch (Exception e) {
			String emsg = e.getClass().getSimpleName() + ": Failed clearing schemas after all tests have run - " + e.getLocalizedMessage();
			System.out.println(emsg);
			System.err.println(emsg);
		}
		// NuoDB END
 
		if ( enableConnectionLeakDetection ) {
			connectionLeakUtil.assertNoLeaks();
		}
	}

	@After
	public void releaseTransactions() {
		if ( JtaStatusHelper.isActive( TestingJtaPlatformImpl.INSTANCE.getTransactionManager() ) ) {
			log.warn( "Cleaning up unfinished transaction" );
			try {
				TestingJtaPlatformImpl.INSTANCE.getTransactionManager().rollback();
			}
			catch (SystemException ignored) {
			}
		}
	}

	protected void sleep(long millis) {
		try {
			Thread.sleep( millis );
		}
		catch ( InterruptedException e ) {
			Thread.interrupted();
		}
	}

	protected Future<?> executeAsync(Runnable callable) {
		return executorService.submit(callable);
	}

	protected void executeSync(Runnable callable) {
		try {
			executeAsync( callable ).get();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		catch (ExecutionException e) {
			throw new RuntimeException( e.getCause() );
		}
	}
}

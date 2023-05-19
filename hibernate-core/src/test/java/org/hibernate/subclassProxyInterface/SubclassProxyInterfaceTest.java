/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.subclassProxyInterface;

import org.hibernate.testing.RequiresDialect;
import org.junit.Test;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.testing.ServiceRegistryBuilder;
import org.hibernate.testing.junit4.BaseUnitTestCase;

/**
 * @author Steve Ebersole
 */
public class SubclassProxyInterfaceTest extends BaseUnitTestCase {
	@Test
	@RequiresDialect(H2Dialect.class)  // NuoDB 18-May-23
	public void testSubclassProxyInterfaces() {
        final Configuration cfg = new Configuration()
				.setProperty( Environment.DIALECT, H2Dialect.class.getName() )
				.addClass( Person.class );
		try (ServiceRegistry serviceRegistry = ServiceRegistryBuilder.buildServiceRegistry( cfg.getProperties() )) {
			cfg.buildSessionFactory( serviceRegistry ).close();
		}
	}
}

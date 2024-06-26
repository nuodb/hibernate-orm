/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.jpa.test.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.EntityType;

import com.nuodb.hibernate.NuoDBDialect;
import org.hibernate.dialect.CockroachDB192Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.jpa.test.BaseEntityManagerFunctionalTestCase;
import org.hibernate.jpa.test.metamodel.Address;
import org.hibernate.jpa.test.metamodel.Alias;
import org.hibernate.jpa.test.metamodel.Country;
import org.hibernate.jpa.test.metamodel.CreditCard;
import org.hibernate.jpa.test.metamodel.Customer;
import org.hibernate.jpa.test.metamodel.Customer_;
import org.hibernate.jpa.test.metamodel.Info;
import org.hibernate.jpa.test.metamodel.LineItem;
import org.hibernate.jpa.test.metamodel.Order;
import org.hibernate.jpa.test.metamodel.Phone;
import org.hibernate.jpa.test.metamodel.Product;
import org.hibernate.jpa.test.metamodel.ShelfLife;
import org.hibernate.jpa.test.metamodel.Spouse;
import org.hibernate.metamodel.internal.MetamodelImpl;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.predicate.ComparisonPredicate;
import org.hibernate.testing.FailureExpected;
import org.hibernate.testing.RequiresDialect;
import org.hibernate.testing.SkipForDialect;
import org.hibernate.testing.TestForIssue;
import org.junit.Test;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.Assert.assertEquals;

/**
 * @author Steve Ebersole
 */
public class QueryBuilderTest extends BaseEntityManagerFunctionalTestCase {
	@Override
	public Class[] getAnnotatedClasses() {
		return new Class[] {
				Address.class,
				Alias.class,
				Country.class,
				CreditCard.class,
				Customer.class,
				Human.class,
				Info.class,
				LineItem.class,
				Order.class,
				Phone.class,
				Product.class,
				ShelfLife.class,
				Spouse.class,
				Book.class,
				Store.class
		};
	}

	@Test
	public void testEqualityComparisonLiteralConversion() {
		EntityManager em = getOrCreateEntityManager();
		em.getTransaction().begin();

		CriteriaBuilderImpl cb = (CriteriaBuilderImpl) em.getCriteriaBuilder();
		MetamodelImpl mm = (MetamodelImpl) em.getMetamodel();

        CriteriaQuery<Integer> cquery = cb.createQuery( Integer.class );
		Root<Product> product = cquery.from( Product.class );
		EntityType<Product> Product_ = mm.entity( Product.class );

		cquery.select(
				cb.toInteger(
						product.get(
								Product_.getSingularAttribute("quantity", Integer.class))
				)
		);

		ComparisonPredicate predicate = (ComparisonPredicate) cb.equal(
				product.get( Product_.getSingularAttribute( "partNumber", Long.class ) ),
				373767373
		);
		assertEquals( Long.class, predicate.getRightHandOperand().getJavaType() );
		cquery.where( predicate );
		em.createQuery( cquery ).getResultList();

		predicate = (ComparisonPredicate) cb.ge(
				cb.length( product.get( Product_.getSingularAttribute( "name", String.class ) ) ),
				4L
		);
		assertEquals( Integer.class, predicate.getRightHandOperand().getJavaType() );
		cquery.where( predicate );
		em.createQuery( cquery ).getResultList();

		em.getTransaction().commit();
		em.close();
	}

	@Test
	public void testEqualityComparisonEntityConversion() {
		EntityManager em = getOrCreateEntityManager();
		em.getTransaction().begin();
		Address address = new Address( "Street Id", "Fake Street", "Fake City", "Fake State", "Fake Zip" );
		Phone phone1 = new Phone( "1", "555", "0001", address );
		Phone phone2 = new Phone( "2", "555", "0002", address );
		Phone phone3 = new Phone( "3", "555", "0003", address );
		Phone phone4 = new Phone( "4", "555", "0004" );

		List<Phone> phones = new ArrayList<Phone>( 3 );
		phones.add( phone1 );
		phones.add( phone2 );
		phones.add( phone3 );

		address.setPhones( phones );
		em.persist( address );
		em.persist( phone4 );

		em.getTransaction().commit();


		em.getTransaction().begin();

		CriteriaBuilderImpl cb = (CriteriaBuilderImpl) em.getCriteriaBuilder();
		MetamodelImpl mm = (MetamodelImpl) em.getMetamodel();
		EntityType<Phone> Phone_ = mm.entity( Phone.class );

		CriteriaQuery<Phone> cquery = cb.createQuery( Phone.class );
		Root<Phone> phone = cquery.from( Phone.class );
		ComparisonPredicate predicate = (ComparisonPredicate) cb.equal(
				phone.get( Phone_.getSingularAttribute( "address", Address.class ) ),
				address
		);
		cquery.where( predicate );
		List<Phone> results = em.createQuery( cquery ).getResultList();

		assertEquals( 3, results.size() );
		em.getTransaction().commit();
		em.close();
	}

	@Test
	public void testTypeConversion() {
		EntityManager em = getOrCreateEntityManager();
		em.getTransaction().begin();
		CriteriaBuilderImpl cb = (CriteriaBuilderImpl) em.getCriteriaBuilder();
		MetamodelImpl mm = (MetamodelImpl) em.getMetamodel();
		EntityType<Product> Product_ = mm.entity( Product.class );

		// toFloat
        CriteriaQuery<Float> floatQuery = cb.createQuery( Float.class );
		Root<Product> product = floatQuery.from( Product.class );
		floatQuery.select(
				cb.toFloat(
						product.get(Product_.getSingularAttribute("quantity", Integer.class))
				)
		);
		em.createQuery( floatQuery ).getResultList();

		// toDouble
        CriteriaQuery<Double> doubleQuery = cb.createQuery(Double.class);
		product = doubleQuery.from( Product.class );
		doubleQuery.select(
				cb.toDouble(
						product.get(Product_.getSingularAttribute("quantity", Integer.class))
				)
		);
		em.createQuery( doubleQuery ).getResultList();

		em.getTransaction().commit();
		em.close();
	}

	@Test
	public void testConstructor() {
		EntityManager em = getOrCreateEntityManager();
		em.getTransaction().begin();
		CriteriaBuilderImpl cb = (CriteriaBuilderImpl) em.getCriteriaBuilder();
		MetamodelImpl mm = (MetamodelImpl) em.getMetamodel();

        CriteriaQuery<Customer> cquery = cb.createQuery(Customer.class);
		Root<Customer> customer = cquery.from(Customer.class);
		EntityType<Customer> Customer_ = customer.getModel();

		cquery.select(
				cb.construct(
						Customer.class,
						customer.get(Customer_.getSingularAttribute("id", String.class)),
						customer.get(Customer_.getSingularAttribute("name", String.class))
				)
		);
		TypedQuery<Customer> tq = em.createQuery(cquery);
		tq.getResultList();

		em.getTransaction().commit();
		em.close();
	}

	@Test
	@TestForIssue(jiraKey = "HHH-8699")
	// For now, restrict to H2.  Selecting w/ predicate functions cause issues for too many dialects.
	@RequiresDialect(value = H2Dialect.class, jiraKey = "HHH-9092")
	public void testMultiselectWithPredicates() {
		EntityManager em = getOrCreateEntityManager();
		em.getTransaction().begin();

		CriteriaBuilderImpl cb = (CriteriaBuilderImpl) em.getCriteriaBuilder();
		CriteriaQuery<Customer> cq = cb.createQuery( Customer.class );
		Root<Customer> r = cq.from( Customer.class );
		cq.multiselect(
				r.get( Customer_.id ), r.get( Customer_.name ),
				cb.concat( "Hello ", r.get( Customer_.name ) ), cb.isNotNull( r.get( Customer_.age ) )
		);
		TypedQuery<Customer> tq = em.createQuery( cq );
		tq.getResultList();

		em.getTransaction().commit();
		em.close();
	}

	@Test
	@SkipForDialect(value = CockroachDB192Dialect.class, strictMatching = true)
	public void testDateTimeFunctions() {
		EntityManager em = getOrCreateEntityManager();
		em.getTransaction().begin();
		CriteriaBuilderImpl cb = (CriteriaBuilderImpl) em.getCriteriaBuilder();
		MetamodelImpl mm = (MetamodelImpl) em.getMetamodel();

        CriteriaQuery<java.sql.Date> dateQuery = cb.createQuery(java.sql.Date.class);
		dateQuery.from( Customer.class );
		dateQuery.select( cb.currentDate() );
		em.createQuery( dateQuery ).getResultList();

        CriteriaQuery<java.sql.Time> timeQuery = cb.createQuery(java.sql.Time.class);
		timeQuery.from( Customer.class );
		timeQuery.select( cb.currentTime() );
		em.createQuery( timeQuery ).getResultList();

        CriteriaQuery<java.sql.Timestamp> tsQuery = cb.createQuery(java.sql.Timestamp.class);
		tsQuery.from( Customer.class );
		tsQuery.select( cb.currentTimestamp() );
		em.createQuery( tsQuery ).getResultList();

		em.getTransaction().commit();
		em.close();
	}

	@Test
	public void testFunctionDialectFunctions() {
		EntityManager em = getOrCreateEntityManager();
		em.getTransaction().begin();
		CriteriaBuilderImpl cb = (CriteriaBuilderImpl) em.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = cb.createQuery( Long.class );
		criteria.select( cb.count( cb.literal( 1 ) ) );
		Root<Customer> root = criteria.from( Customer.class );
		criteria.where(
				cb.equal(
						cb.function(
								"substring",
								String.class,
								root.get( Customer_.name ),
								cb.literal( 1 ),
								cb.literal( 1 )
						),
						cb.literal( "a" )
				)
		);
		em.createQuery( criteria ).getResultList();
		em.getTransaction().commit();
		em.close();
	}
	
	@Test
	@TestForIssue(jiraKey = "HHH-10737")
	@FailureExpected( jiraKey = "HHH-10737" )
	public void testMissingDialectFunction() {
		doInJPA( this::entityManagerFactory, em -> {
			Human human = new Human();
			human.setId(200L);
			human.setName("2");
			human.setBorn(new Date());
			em.persist(human);

			em.getTransaction().commit();

			CriteriaBuilder cb =  em.getCriteriaBuilder();
			CriteriaQuery<HumanDTO> criteria = cb.createQuery( HumanDTO.class );
			Root<Human> root = criteria.from( Human.class );

			criteria.select(
				cb.construct(
					HumanDTO.class,
					root.get(Human_.id),
					root.get(Human_.name),
					cb.function(
						"convert",
						String.class,
						root.get(Human_.born),
						cb.literal(110)
					)
				)
			);

			em.createQuery( criteria ).getResultList();
		} );
	}

	@Test
	@TestForIssue(jiraKey = "HHH-12314")
	// TODO: NuoDB: 18-May-23 - Parentheses in JOIN statement
	@SkipForDialect(value= NuoDBDialect.class,
	    comment = "NuoDB expects SELECT in generated JOIN sub-select")
	public void testJoinUsingNegatedPredicate() {
		// Write test data
		doInJPA( this::entityManagerFactory, entityManager -> {
			final Store store = new Store();
			store.setName( "Acme Books" );
			store.setAddress( "123 Main St" );
			entityManager.persist( store );

			final Book book = new Book();
			book.setStores( new HashSet<>( Arrays.asList( store ) ) );
			entityManager.persist( book );
		} );

		doInJPA( this::entityManagerFactory, entityManager -> {
			final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			final CriteriaQuery<Book> query = cb.createQuery( Book.class );
			final Root<Book> bookRoot = query.from( Book.class );

			SetJoin<Book, Store> storeJoin = bookRoot.join( Book_.stores );
			storeJoin.on( cb.isNotNull( storeJoin.get( Store_.address ) ) );

			// Previously failed due to ClassCastException
			// org.hibernate.query.criteria.internal.predicate.NegatedPredicateWrapper
			//   cannot be cast to
			// org.hibernate.query.criteria.internal.predicate.AbstractPredicateImpl
			entityManager.createQuery( query ).getResultList();
		} );
	}
}

package org.hibernate.bugs;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.bugs.model.CurrentUser;
import org.hibernate.bugs.model.GeneratedRegular;
import org.hibernate.bugs.model.GeneratedWithIdentity;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.PostgreSQLDialect;

import org.junit.Before;
import org.junit.Test;

/**
 * This template demonstrates how to develop a standalone test case for Hibernate ORM.  Although this is perfectly
 * acceptable as a reproducer, usage of ORMUnitTestCase is preferred!
 */
public class ORMStandaloneTestCase {

	private SessionFactory sf;

	@Before
	public void setup() {
		StandardServiceRegistryBuilder srb = new StandardServiceRegistryBuilder()
				// Add in any settings that are specific to your test. See resources/hibernate.properties for the defaults.
				.applySetting( "hibernate.show_sql", "true" )
				.applySetting( "hibernate.format_sql", "true" )
				.applySetting( "hibernate.hbm2ddl.auto", "update" );

		Metadata metadata = new MetadataSources( srb.build() )
				// Add your entities here.
				.addAnnotatedClass( GeneratedRegular.class )
				.addAnnotatedClass( GeneratedWithIdentity.class )
				.buildMetadata();

		sf = metadata.buildSessionFactory();
	}

	// Add your tests, using standard JUnit.

	@Test
	public void hhhTest() {
		final GeneratedWithIdentity davide = new GeneratedWithIdentity( "Davide", "D'Alto" );
		CurrentUser.INSTANCE.logIn( "dd-insert" );
		try {
			try (Session session = sf.openSession() ){
				session.beginTransaction();
				session.persist( davide );
				session.getTransaction().commit();
			}
		}
		finally {
			sf.close();
		}
	}
}

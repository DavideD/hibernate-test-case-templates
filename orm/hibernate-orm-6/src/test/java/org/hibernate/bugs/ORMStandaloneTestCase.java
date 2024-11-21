package org.hibernate.bugs;

import java.math.BigDecimal;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import static org.hibernate.cfg.JdbcSettings.DRIVER;

/**
 * This template demonstrates how to develop a standalone test case for Hibernate ORM.  Although this is perfectly
 * acceptable as a reproducer, usage of ORMUnitTestCase is preferred!
 */
class ORMStandaloneTestCase {

	protected SessionFactory constructFactory(String action) {
		StandardServiceRegistryBuilder srb = new StandardServiceRegistryBuilder()
				.applySetting( "hibernate.show_sql", "true" )
				.applySetting( "hibernate.format_sql", "true" )
				.applySetting( "hibernate.hbm2ddl.auto", action )
				;

		Metadata metadata = new MetadataSources( srb.build() )
				.addAnnotatedClass( Foo.class )
				.buildMetadata();

		return metadata.buildSessionFactory();
	}

	@Test
	public void testOrmValidation() {
		constructFactory( "create" ).close();
		constructFactory( "validate" ).close();
	}

	@Entity
	public static class Foo {
		@Id
		public Integer id;
		public BigDecimal[] bigDecimals;
	}
}

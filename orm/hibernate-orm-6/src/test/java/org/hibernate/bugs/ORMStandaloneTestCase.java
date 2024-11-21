package org.hibernate.bugs;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import static org.assertj.core.api.Assertions.assertThat;

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
//				.applySetting( "hibernate.type.preferred_array_jdbc_type", "VARBINARY" )
				;

		Metadata metadata = new MetadataSources( srb.build() )
				.addAnnotatedClass( Foo.class )
				.buildMetadata();

		return metadata.buildSessionFactory();
	}

	@Test
	@Order( 1 )
	public void testLocalDateTime() {
		LocalDateTime[] dataArray = {
				// Unix epoch start if you're in the UK
				LocalDateTime.of( 1970, Month.JANUARY, 1, 0, 0, 0, 0 ),
				// pre-Y2K
				LocalDateTime.of( 1999, Month.DECEMBER, 31, 23, 59, 59, 0 ),
				// We survived! Why was anyone worried?
				LocalDateTime.of( 2000, Month.JANUARY, 1, 0, 0, 0, 0 ),
				// Silence will fall!
				LocalDateTime.of( 2010, Month.JUNE, 26, 20, 4, 0, 0 )
		};
		try (SessionFactory sf = constructFactory( "create" )) {
			Foo basic = new Foo();
			basic.localDateTimeArray = dataArray;
			try (Session session = sf.openSession()) {
				session.beginTransaction();
				session.persist( basic );
				session.getTransaction().commit();
			}
			try (Session session = sf.openSession()) {
				session.beginTransaction();
				Foo found = session.find( Foo.class, basic.id );
				assertThat( found.localDateTimeArray ).isEqualTo( dataArray );
				session.getTransaction().commit();
			}
		}
	}


	@Test
	@Order( 2 )
	public void testDate() {
		Date[] dataArray = {Calendar.getInstance().getTime(), Calendar.getInstance().getTime()};

		try (SessionFactory sf = constructFactory( "update" )) {
			Foo basic = new Foo();
			basic.dateArray = dataArray;
			try (Session session = sf.openSession()) {
				session.beginTransaction();
				session.persist( basic );
				session.getTransaction().commit();
			}
			try (Session session = sf.openSession()) {
				session.beginTransaction();
				Foo found = session.find( Foo.class, basic.id );
				for ( int i = 0; i < dataArray.length; i++ ) {
					assertThat( found.dateArray[i].getTime() ).isEqualTo( dataArray[i].getTime() );
				}
				session.getTransaction().commit();
			}
		}
	}

	@Entity
	public static class Foo {
		@Id
		@GeneratedValue
		public Integer id;
		public Date[] dateArray;
		public LocalDateTime[] localDateTimeArray;
	}
}

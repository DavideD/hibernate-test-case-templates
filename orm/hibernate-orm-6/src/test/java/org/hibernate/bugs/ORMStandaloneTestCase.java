package org.hibernate.bugs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

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
				.addAnnotatedClass( Thing.class )
				.buildMetadata();

		sf = metadata.buildSessionFactory();
	}

	// Add your tests, using standard JUnit.
	final Thing thing = new Thing();

	@Test
	public void testORM() {
		thing.localDateTime = LocalDateTime.now()
				.truncatedTo( ChronoUnit.MILLIS );

		try (Session session = sf.openSession()) {
			session.beginTransaction();
			session.persist( thing );
			session.getTransaction().commit();
		}

		try (Session session = sf.openSession()) {
			List<Thing> dt = session.createSelectionQuery( "from Thing where localDateTimeType=:dt", Thing.class )
					.setParameter( "dt", thing.getLocalDateTime() )
					.list();
			Assertions.assertNotNull( dt );
		}
	}


	@Entity(name = "Thing")
	public static class Thing {
		@Id
		@GeneratedValue
		long id;

		@Column(name = "dateType")
		Date date;

		@Column(name = "calendarType")
		Calendar calendar;

		@Column(name = "offsetDateTimeType")
		OffsetDateTime offsetDateTime;

		@Column(name = "offsetTimeType")
		OffsetTime offsetTime;

		@Column(name = "zonedDateTimeType")
		ZonedDateTime zonedDateTime;

		@Column(name = "localDateType")
		LocalDate localDate;

		@Column(name = "localTimeType")
		LocalTime localTime;

		@Column(name = "localDateTimeType")
		LocalDateTime localDateTime;

		public Date getDate() {
			return date;
		}

		public Calendar getCalendar() {
			return calendar;
		}

		public OffsetDateTime getOffsetDateTime() {
			return offsetDateTime;
		}

		public OffsetTime getOffsetTime() {
			return offsetTime;
		}

		public ZonedDateTime getZonedDateTime() {
			return zonedDateTime;
		}

		public LocalDate getLocalDate() {
			return localDate;
		}

		public LocalTime getLocalTime() {
			return localTime;
		}

		public LocalDateTime getLocalDateTime() {
			return localDateTime;
		}
	}
}

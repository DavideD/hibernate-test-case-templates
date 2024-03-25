package org.hibernate.bugs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.EnumSet;
import java.util.Objects;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.ValueGenerationType;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.SchemaToolingSettings;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;

import org.junit.Before;
import org.junit.Test;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

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
			.applySetting( SchemaToolingSettings.HBM2DDL_CREATE_SOURCE, "script-then-metadata" )
			.applySetting( SchemaToolingSettings.HBM2DDL_CREATE_SCRIPT_SOURCE, "/mysql-pipe.sql" )
			.applySetting( "hibernate.show_sql", "true" )
			.applySetting( "hibernate.format_sql", "true" )
			.applySetting( "hibernate.hbm2ddl.auto", "create" );

		Metadata metadata = new MetadataSources( srb.build() )
		// Add your entities here.
			.addAnnotatedClass( GeneratedWithIdentity.class )
			.addAnnotatedClass( GeneratedRegular.class )
			.buildMetadata();

		sf = metadata.buildSessionFactory();
	}

	// Add your tests, using standard JUnit.

	@Test
	public void testWithIdentity() {
		final GeneratedWithIdentity davide = new GeneratedWithIdentity( "Davide", "D'Alto" );

		CurrentUser.INSTANCE.logIn( "dd-insert" );

		try (Session session = sf.openSession()) {
			session.beginTransaction();
			session.persist( davide );
			session.getTransaction().commit();
		}
	}


	@Entity(name = "GeneratedRegular")
	@Table(name = "GeneratedRegularSingleTable")
	static class GeneratedRegular {
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		public Long id;

		public String firstname;

		public String lastname;

		@Generated(GenerationTime.ALWAYS)
		@Column(columnDefinition = "varchar(600) generated always as (firstname || ' ' || lastname) stored")
		private String fullName;

		@Temporal(value = TemporalType.TIMESTAMP)
		@Generated(GenerationTime.INSERT)
		@Column(columnDefinition = "timestamp")
		@ColumnDefault("current_timestamp")
		public Date createdAt;

		@CurrentUser.LoggedUserInsert
		public String createdBy;

		@CurrentUser.LoggedUserAlways
		public String updatedBy;

		@Generated(GenerationTime.NEVER)
		public String never;

		public GeneratedRegular() {
		}

		public GeneratedRegular(String firstname, String lastname) {
			this.firstname = firstname;
			this.lastname = lastname;
		}
	}

	@Entity(name = "GeneratedWithIdentity")
	@Table(name = "GeneratedWithIdentitySingleTable")
	static class GeneratedWithIdentity {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		public Long id;

		public String firstname;

		public String lastname;

		@Generated(GenerationTime.ALWAYS)
		@Column(columnDefinition = "varchar(600) generated always as (firstname || ' ' || lastname) stored")
		private String fullName;

		@Temporal(value = TemporalType.TIMESTAMP)
		@Generated(GenerationTime.INSERT)
		@Column(columnDefinition = "timestamp")
		@ColumnDefault("current_timestamp")
		public Date createdAt;

		@CurrentUser.LoggedUserInsert
		public String createdBy;

		@CurrentUser.LoggedUserAlways
		public String updatedBy;

		@Generated(GenerationTime.NEVER)
		public String never;

		public GeneratedWithIdentity() {
		}

		public GeneratedWithIdentity(String firstname, String lastname) {
			this.firstname = firstname;
			this.lastname = lastname;
		}
	}

	public static class CurrentUser {

		public static final CurrentUser INSTANCE = new CurrentUser();

		private static final ThreadLocal<String> storage = new ThreadLocal<>();

		public void logIn(String user) {
			storage.set( user );
		}

		public void logOut() {
			storage.remove();
		}

		public String get() {
			return storage.get();
		}

		@ValueGenerationType(generatedBy = InsertLoggedUserGenerator.class)
		@Retention(RetentionPolicy.RUNTIME)
		public @interface LoggedUserInsert {
		}

		@ValueGenerationType(generatedBy = AlwaysLoggedUserGenerator.class)
		@Retention(RetentionPolicy.RUNTIME)
		public @interface LoggedUserAlways {
		}

		public static class InsertLoggedUserGenerator implements BeforeExecutionGenerator {

			@Override
			public EnumSet<EventType> getEventTypes() {
				return EventTypeSets.INSERT_ONLY;
			}

			@Override
			public Object generate(
					SharedSessionContractImplementor session,
					Object owner,
					Object currentValue,
					EventType eventType) {
				Objects.requireNonNull( session );
				return CurrentUser.INSTANCE.get();
			}
		}

		public static class AlwaysLoggedUserGenerator implements BeforeExecutionGenerator {

			@Override
			public Object generate(
					SharedSessionContractImplementor session,
					Object owner,
					Object currentValue,
					EventType eventType) {
				Objects.requireNonNull( session );
				return CurrentUser.INSTANCE.get();
			}

			@Override
			public EnumSet<EventType> getEventTypes() {
				return EventTypeSets.ALL;
			}
		}
	}
}

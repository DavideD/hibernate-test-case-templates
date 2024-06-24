package org.hibernate.bugs;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

import org.junit.Before;
import org.junit.Test;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
				.applySetting( "hibernate.hbm2ddl.auto", "create" )
				.applySetting( AvailableSettings.JAKARTA_JDBC_URL, "jdbc:postgresql://localhost:5432/hreact" )
				.applySetting( AvailableSettings.JAKARTA_JDBC_USER, "hreact" )
				.applySetting( AvailableSettings.JAKARTA_JDBC_PASSWORD, "hreact" )
				.applySetting( AvailableSettings.JAKARTA_JDBC_DRIVER, "org.postgresql.Driver" )
				;

		Metadata metadata = new MetadataSources( srb.build() )
				// Add your entities here.
				.addAnnotatedClass( AcademicYearDetailsDBO.class )
				.addAnnotatedClass( CampusDBO.class )

				.buildMetadata();

		sf = metadata.buildSessionFactory();
	}

	// Add your tests, using standard JUnit.

	@Test
	public void hhh123Test() throws Exception {
		CampusDBO campusDBO2 = new CampusDBO();
		campusDBO2.setId( 42 );
		campusDBO2.setCampusName( "Kuchl" );

		CampusDBO campusDBO = new CampusDBO();
		campusDBO.setId( 66 );
		campusDBO.setCampusName( "Qualunquelandia" );

		AcademicYearDetailsDBO academicYearDetailsDBO = new AcademicYearDetailsDBO();
		academicYearDetailsDBO.setId( 69 );
		academicYearDetailsDBO.setCampusDBO( campusDBO );
		academicYearDetailsDBO.setCreatedUsersId( 12 );
		academicYearDetailsDBO.setRecordStatus( 'F' );
		academicYearDetailsDBO.setModifiedUsersId( 66 );

		try (Session ormSession = sf.openSession()) {
			ormSession.beginTransaction();
			ormSession.persist( campusDBO );
			ormSession.persist( campusDBO2 );
			ormSession.persist( academicYearDetailsDBO );
			ormSession.getTransaction().commit();
		}
		try (Session ormSession = sf.openSession()) {
			ormSession.beginTransaction();
			ormSession.createSelectionQuery( "from AcademicYearDetailsDBO dbo", AcademicYearDetailsDBO.class ).getSingleResult();
			ormSession.getTransaction().commit();
		}
	}


	@Entity(name = "AcademicYearDetailsDBO")
	@Table(name = "erp_academic_year_detail")
	static class AcademicYearDetailsDBO implements Serializable {
		@Id
		@Column(name = "erp_academic_year_detail_id")
		private Integer id;

		@ManyToOne(fetch = FetchType.EAGER)
		@JoinColumn(name = "erp_campus_id")
		private CampusDBO campusDBO;

		@Column(name = "record_status")
		private char recordStatus;

		@Column(name = "created_users_id")
		private Integer createdUsersId;

		@Column(name = "modified_users_id")
		private Integer modifiedUsersId;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public CampusDBO getCampusDBO() {
			return campusDBO;
		}

		public void setCampusDBO(CampusDBO campusDBO) {
			this.campusDBO = campusDBO;
		}

		public char getRecordStatus() {
			return recordStatus;
		}

		public void setRecordStatus(char recordStatus) {
			this.recordStatus = recordStatus;
		}

		public Integer getCreatedUsersId() {
			return createdUsersId;
		}

		public void setCreatedUsersId(Integer createdUsersId) {
			this.createdUsersId = createdUsersId;
		}

		public Integer getModifiedUsersId() {
			return modifiedUsersId;
		}

		public void setModifiedUsersId(Integer modifiedUsersId) {
			this.modifiedUsersId = modifiedUsersId;
		}
	}

	@Entity(name = "CampusDBO")
	@Table(name = "erp_campus")
	static class CampusDBO implements Serializable {
		@Id
		@Column(name = "erp_campus_id")
		private Integer id;

		@Column(name = "campus_name")
		private String campusName;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getCampusName() {
			return campusName;
		}

		public void setCampusName(String campusName) {
			this.campusName = campusName;
		}
	}
}

package org.hibernate.bugs;

import java.math.BigInteger;

import org.hibernate.SessionFactory;
import org.hibernate.annotations.Array;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

import org.junit.Before;
import org.junit.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import static org.assertj.core.api.Assertions.assertThat;

public class BigIntegerArrayTest {

	private SessionFactory sf;

	@Test
	public void hhh123Test() {
		createFactory( "drope" ).close();
		createFactory( "update" ).close();
	}

	private static SessionFactory createFactory(String auto) {
		StandardServiceRegistryBuilder srb = new StandardServiceRegistryBuilder()
				// Add in any settings that are specific to your test. See resources/hibernate.properties for the defaults.
				.applySetting( "hibernate.show_sql", "true" )
				.applySetting( "hibernate.format_sql", "true" )
				.applySetting( "hibernate.hbm2ddl.auto", auto )
				.applySetting( AvailableSettings.FORMAT_SQL, "false" )
				.applySetting( AvailableSettings.HIGHLIGHT_SQL, "true" );

		Metadata metadata = new MetadataSources( srb.build() )
				// Add your entities here.
				.addAnnotatedClass( BasicTypesTestEntity.class )
				.buildMetadata();

		return metadata.buildSessionFactory();
	}

	@Entity(name = "BasicTypesTestEntity")
	@Table(name = BasicTypesTestEntity.TABLE_NAME)
	public static class BasicTypesTestEntity {

		public static final String TABLE_NAME = "BASIC_TYPES_TABLE";

		String name;

		@Id
		@GeneratedValue
		Integer id;
		@Version
		Integer version;

		BigInteger[] bigIntegerArray;

		@Array(length = 5)
		BigInteger[] bigIntegerArrayAnnotated;

		public BasicTypesTestEntity() {
		}
		public BasicTypesTestEntity(String name) {
			this.name = name;
		}
	}


}

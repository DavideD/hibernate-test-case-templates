package org.hibernate.bugs;

import java.util.Objects;

import org.hibernate.SessionFactory;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.type.SqlTypes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This template demonstrates how to develop a standalone test case for Hibernate ORM.  Although this is perfectly
 * acceptable as a reproducer, usage of ORMUnitTestCase is preferred!
 */
class ORMStandaloneTestCase {

	private SessionFactory sf;

	@BeforeEach
	void setup() {
		StandardServiceRegistryBuilder srb = new StandardServiceRegistryBuilder()
				.applySetting( "hibernate.show_sql", "true" )
				.applySetting( "hibernate.format_sql", "true" )
				.applySetting( "hibernate.hbm2ddl.auto", "create" );

		Metadata metadata = new MetadataSources( srb.build() )
				.addAnnotatedClass( Book.class )
				.buildMetadata();

		sf = metadata.buildSessionFactory();
	}

	@Test
	void hhh123Test() {
		final Book fakeHistory = new Book( 3, "Fake History", new Book.Author( "Jo", 11L ) );
		sf.inTransaction( session -> session.persist( fakeHistory ) );
		sf.inTransaction( session -> {
			Book result = session
					.createNativeQuery( "select * from BookWithJson", Book.class )
					.getSingleResult();
			assertThat( result ).isEqualTo( fakeHistory );
		} );
	}

	@Entity(name = "Book")
	@Table(name = "BookWithJson")
	public static class Book {

		@Id
		Integer id;

		String title;

		@JdbcTypeCode(SqlTypes.JSON)
		Book.Author author;

		public Book() {
		}

		public Book(Integer id, String title, Book.Author author) {
			this.id = id;
			this.title = title;
			this.author = author;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}
			Book book = (Book) o;
			return Objects.equals( id, book.id ) && Objects.equals(
					title,
					book.title
			) && Objects.equals( author, book.author );
		}

		@Override
		public int hashCode() {
			return Objects.hash( id, title, author );
		}

		@Override
		public String toString() {
			return id + ":" + title + ":" + author;
		}

		@Embeddable
		public static class Author {
			private String name;
			private Long age;

			public Author() {
			}

			public Author(String name, Long age) {
				this.name = name;
				this.age = age;
			}

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public Long getAge() {
				return age;
			}

			public void setAge(Long age) {
				this.age = age;
			}

			@Override
			public boolean equals(Object o) {
				if ( this == o ) {
					return true;
				}
				if ( o == null || getClass() != o.getClass() ) {
					return false;
				}
				Book.Author author = (Book.Author) o;
				return Objects.equals( name, author.name ) && Objects.equals( age, author.age );
			}

			@Override
			public int hashCode() {
				return Objects.hash( name, age );
			}

			@Override
			public String toString() {
				return name + ' ' + age;
			}
		}
	}
}

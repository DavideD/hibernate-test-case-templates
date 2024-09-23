/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.bugs;

import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.PostgreSQLJsonPGObjectJsonbType;
import org.hibernate.type.SqlTypes;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.hibernate.testing.orm.junit.Setting;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

import static org.assertj.core.api.Assertions.assertThat;

@DomainModel(annotatedClasses = QueriesWithEmbeddedJsonTest.Book.class)
@ServiceRegistry(
		// Add in any settings that are specific to your test.  See resources/hibernate.properties for the defaults.
		settings = {
				// For your own convenience to see generated queries:
				@Setting(name = AvailableSettings.SHOW_SQL, value = "true"),
				@Setting(name = AvailableSettings.FORMAT_SQL, value = "true"),
				@Setting(name = AvailableSettings.HBM2DDL_AUTO, value = "create"),
				@Setting(name = AvailableSettings.DIALECT, value = "org.hibernate.dialect.PostgreSQLDialect"),
				@Setting(name = AvailableSettings.JAKARTA_JDBC_DRIVER, value = "org.postgresql.Driver"),
				@Setting(name = AvailableSettings.JAKARTA_JDBC_URL, value = "jdbc:postgresql://localhost:5432/hreact"),
				@Setting(name = AvailableSettings.JAKARTA_JDBC_USER, value = "hreact"),
				@Setting(name = AvailableSettings.JAKARTA_JDBC_PASSWORD, value = "hreact"),
		}
)
@SessionFactory
public class QueriesWithEmbeddedJsonTest {

	private static final Book fakeHistory = new Book( 3, "Fake History", new Book.Author( "Jo", "Hedwig Teeuwisse" ) );
	private static final  Book theBookOfM = new Book( 5, "The Book of M", new Book.Author( "Peng", "Shepherd" ) );

	@BeforeEach
	public void populateDb(SessionFactoryScope scope) {
		scope.inTransaction( s -> {
			s.persist( fakeHistory );
			s.persist( theBookOfM );
		} );
	}

	@AfterEach
	public void cleanDb(SessionFactoryScope scope) {
		scope.inTransaction( s -> s.createMutationQuery( "delete from Book" ).executeUpdate() );
	}

	@Test
	public void selectAllNative(SessionFactoryScope scope) {
		scope.inTransaction( session -> {
			List<Book> results = session.createNativeQuery( "select * from BookWithJson", Book.class )
					.getResultList();
			assertThat( results ).containsExactlyInAnyOrder( theBookOfM, fakeHistory );
		} );
	}

	@Test
	public void selectAllJpql(SessionFactoryScope scope) {
		scope.inTransaction( session -> {
			List<Book> results = session.createSelectionQuery( "from Book", Book.class )
					.getResultList();
			assertThat( results ).containsExactlyInAnyOrder( theBookOfM, fakeHistory );
		} );
	}

	@Test
	public void selectAllCriteria(SessionFactoryScope scope) {
		CriteriaBuilder cb = scope.getSessionFactory().getCriteriaBuilder();
		CriteriaQuery<Book> query = cb.createQuery( Book.class );
		query.from( Book.class );
		scope.inTransaction( session -> {
			List<Book> results = session.createQuery( query ).getResultList();
			assertThat( results ).containsExactlyInAnyOrder( theBookOfM, fakeHistory );
		} );
	}

	@Entity(name = "Book")
	@Table(name = "BookWithJson")
	public static class Book {

		@Id
		Integer id;

		String title;

		@JdbcTypeCode(SqlTypes.JSON)
		@JdbcType(PostgreSQLJsonPGObjectJsonbType.class)
		Author author;

		@Embeddable
		public static class Author {
			private String name;
			private String surname;

			public Author() {
			}

			public Author(String name, String surname) {
				this.name = name;
				this.surname = surname;
			}

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public String getSurname() {
				return surname;
			}

			public void setSurname(String surname) {
				this.surname = surname;
			}

			@Override
			public boolean equals(Object o) {
				if ( this == o ) {
					return true;
				}
				if ( o == null || getClass() != o.getClass() ) {
					return false;
				}
				Author author = (Author) o;
				return Objects.equals( name, author.name ) && Objects.equals( surname, author.surname );
			}

			@Override
			public int hashCode() {
				return Objects.hash( name, surname );
			}

			@Override
			public String toString() {
				return name + ' ' + surname;
			}
		}

		public Book() {
		}

		public Book(Integer id, String title, Author author) {
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
	}
}

package org.hibernate.bugs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.cfg.AvailableSettings;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.hibernate.testing.orm.junit.Setting;
import org.junit.jupiter.api.Test;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REFRESH;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.annotations.FetchMode.SUBSELECT;
import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using its built-in unit test framework.
 * Although ORMStandaloneTestCase is perfectly acceptable as a reproducer, usage of this class is much preferred.
 * Since we nearly always include a regression test with bug fixes, providing your reproducer using this method
 * simplifies the process.
 * <p>
 * What's even better?  Fork hibernate-orm itself, add your test case directly to a module's unit tests, then
 * submit it as a PR!
 */
@DomainModel(annotatedClasses = { ORMUnitTestCase.Element.class, ORMUnitTestCase.Node.class })
@ServiceRegistry(
		// Add in any settings that are specific to your test.  See resources/hibernate.properties for the defaults.
		settings = {
				// For your own convenience to see generated queries:
				@Setting(name = AvailableSettings.SHOW_SQL, value = "true"),
				@Setting(name = AvailableSettings.FORMAT_SQL, value = "true"),
				// @Setting( name = AvailableSettings.GENERATE_STATISTICS, value = "true" ),

				// Add your own settings that are a part of your quarkus configuration:
				// @Setting( name = AvailableSettings.SOME_CONFIGURATION_PROPERTY, value = "SOME_VALUE" ),
		}
)
@SessionFactory
class ORMUnitTestCase {

	// Add your tests, using standard JUnit 5.
	@Test
	void testNullPointer(SessionFactoryScope scope) throws Exception {
		Node basik = new Node( "Child" );
		basik.parent = new Node( "Parent" );
		basik.elements.add( new Element( basik ) );
		basik.elements.add( new Element( basik ) );
		basik.elements.add( new Element( basik ) );

		scope.inTransaction( session -> session.persist( basik ) );
		scope.inTransaction( session -> {
			List<Node> resultList = session
					.createSelectionQuery( "select distinct n from Node n left join fetch n.elements", Node.class )
					.getResultList();
			assertThat( resultList ).hasSize( 2 );
		} );
		scope.inTransaction( session -> {
			List<Node> resultList = session
					.createSelectionQuery( "select distinct n, e from Node n join n.elements e", Node.class )
					.getResultList();
			assertThat( resultList ).hasSize( 3 );
		} );
		scope.inTransaction( session -> {
			List<Node> resultList = session
					.createSelectionQuery( "select distinct n.id, e.id from Node n join n.elements e", Node.class )
					.getResultList();
			assertThat( resultList ).hasSize( 3 );
		} );
		scope.inTransaction( session -> {
			List<Node> resultList = session
					.createSelectionQuery( "select max(e.id), min(e.id), sum(e.id) from Node n join n.elements e group by n.id order by n.id", Node.class )
					.getResultList();
			assertThat( resultList ).hasSize( 1 );
		} );
	}

	@Test
	void testEagerFetchQuery(SessionFactoryScope scope) throws Exception {
		Node basik = new Node( "Child" );
		basik.parent = new Node( "Parent" );
		basik.elements.add( new Element( basik ) );
		basik.elements.add( new Element( basik ) );
		basik.elements.add( new Element( basik ) );

		scope.inTransaction( session -> session.persist( basik ) );
		scope.inTransaction( session -> {
			List<Node> list = session.createSelectionQuery( "from Node order by id", Node.class ).getResultList();
			assertThat( list ).hasSize( 2 );
			assertThat( Hibernate.isInitialized( list.get( 0 ).elements ) ).isTrue();
			assertThat( list.get( 0 ).elements ).hasSize( 3 );
			assertThat( list.get( 1 ).elements ).isEmpty();
		} );

		scope.inTransaction( session -> {
			List<Object[]> list = session.createSelectionQuery( "select distinct n, e from Node n join n.elements e order by n.id", Object[].class ).getResultList();
			assertThat( list ).hasSize( 3 );
			Object[] tup = list.get( 0 );
			assertTrue( Hibernate.isInitialized( ( (Node) tup[0] ).elements ) );
			assertThat(  ( (Node) tup[0] ).elements ).hasSize( 3 );
		} );
	}

	@Entity(name = "Element")
	@Table(name = "Element")
	public static class Element {
		@Id
		@GeneratedValue
		Integer id;

		@ManyToOne
		@Fetch(FetchMode.SELECT)
		Node node;

		public Element(Node node) {
			this.node = node;
		}

		public Element() {
		}
	}

	@Entity(name = "Node")
	@Table(name = "Node")
	public static class Node {

		@Id
		@GeneratedValue
		Integer id;
		@Version
		Integer version;
		String string;
		@Transient
		boolean loaded = false;

		@ManyToOne(fetch = LAZY, cascade = { PERSIST, REFRESH, MERGE, REMOVE })
		Node parent;

		@OneToMany(fetch = EAGER, cascade = { PERSIST, REMOVE }, mappedBy = "node")
		@Fetch(SUBSELECT)
		List<Element> elements = new ArrayList<>();

		public Node(String string) {
			this.string = string;
		}

		public Node() {
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}

		@PostLoad
		void postLoad() {
			loaded = true;
		}

		@Override
		public String toString() {
			return id + ": " + string;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}
			Node node = (Node) o;
			return Objects.equals( string, node.string );
		}

		@Override
		public int hashCode() {
			return Objects.hash( string );
		}
	}
}

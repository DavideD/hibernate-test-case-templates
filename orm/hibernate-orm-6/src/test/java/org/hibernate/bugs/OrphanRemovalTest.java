package org.hibernate.bugs;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

import org.junit.Before;
import org.junit.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static org.assertj.core.api.Assertions.assertThat;

public class OrphanRemovalTest {

	private SessionFactory sf;

	@Before
	public void setup() {
		StandardServiceRegistryBuilder srb = new StandardServiceRegistryBuilder()
				// Add in any settings that are specific to your test. See resources/hibernate.properties for the defaults.
				.applySetting( "hibernate.show_sql", "true" )
				.applySetting( "hibernate.format_sql", "true" )
				.applySetting( "hibernate.hbm2ddl.auto", "create" )
				.applySetting( AvailableSettings.FORMAT_SQL, "false" )
				.applySetting( AvailableSettings.HIGHLIGHT_SQL, "true" );

		Metadata metadata = new MetadataSources( srb.build() )
				// Add your entities here.
				.addAnnotatedClass( Shop.class )
				.addAnnotatedClass( Product.class )
				.addAnnotatedClass( Version.class )
				.buildMetadata();

		sf = metadata.buildSessionFactory();
	}

	@Test
	public void hhh123Test() {
		Shop shop = new Shop( "shop" );
		Product product = new Product( "ap1" );
		product.addVersion( new Version() );
		shop.addProduct( product );
		shop.addProduct( new Product( "ap2" ) );
		shop.addProduct( new Product( "ap3" ) );
		shop.addProduct( new Product( "ap4" ) );

		try (Session session = sf.openSession()) {
			session.beginTransaction();
			session.persist( shop );
			session.getTransaction().commit();
		}
		try (Session session = sf.openSession()) {
			session.beginTransaction();
			Shop result = session.find( Shop.class, shop.id );
			result.removeAllProducts();
			result.addProduct( new Product( "bp5" ) );
			result.addProduct( new Product( "bp6" ) );
			result.addProduct( new Product( "bp7" ) );
			session.getTransaction().commit();
		}
		try (Session session = sf.openSession()) {
			session.beginTransaction();
			List<String> result = session
					.createSelectionQuery( "select name from Product", String.class )
					.getResultList();
			assertThat( result ).containsExactlyInAnyOrder( "bp5", "bp6", "bp7" );
			session.getTransaction().commit();
		}
	}

	@Entity(name = "Version")
	@Table(name = "ORT_ProductVersion")
	public static class Version {
		@Id
		@GeneratedValue
		private long id;

		@ManyToOne
		private Product product;

		public Version() {
		}

		@Override
		public String toString() {
			return Version.class.getSimpleName() + ":" + id + ":" + product;
		}
	}

	@Entity(name = "Product")
	@Table(name = "ORT_Product")
	public static class Product {

		@Id
		@GeneratedValue
		private long id;
		private String name;

		public Product() {
		}

		public Product(String name) {
			this.name = name;
		}

		@ManyToOne
		private Shop shop;

		@OneToMany(mappedBy = "product", cascade = {PERSIST, REMOVE})
		private Set<Version> versions = new HashSet<>();

		public void addVersion(Version version) {
			versions.add( version );
			version.product = this;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}
			Product product = (Product) o;
			return Objects.equals( name, product.name ) && Objects.equals( shop, product.shop );
		}

		@Override
		public int hashCode() {
			return Objects.hash( name, shop );
		}

		@Override
		public String toString() {
			return Product.class.getSimpleName() + ":" + id + ":" + name + ":" + shop;
		}
	}

	@Entity(name = "Shop")
	@Table(name = "ORT_Shop")
	public static class Shop {

		@Id
		@GeneratedValue
		private long id;
		private String name;

		public Shop() {
		}

		public Shop(String name) {
			this.name = name;
		}

		@OneToMany(mappedBy = "shop", cascade = {PERSIST, REMOVE}, orphanRemoval = true) // cascade ALL will actually work
		private Set<Product> products = new HashSet<>();

		public void addProduct(Product product) {
			products.add( product );
			product.shop = this;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Set<Product> getProducts() {
			return products;
		}

		public void setProducts(Set<Product> products) {
			this.products = products;
		}

		public void removeAllProducts() {
			products.forEach( p -> p.shop = null );
			products.clear();
		}

		@Override
		public int hashCode() {
			return Objects.hashCode( name );
		}

		@Override
		public String toString() {
			return Shop.class.getSimpleName() + ":" + id + ":" + name;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}
			Shop shop = (Shop) o;
			return Objects.equals( name, shop.name );
		}
	}
}

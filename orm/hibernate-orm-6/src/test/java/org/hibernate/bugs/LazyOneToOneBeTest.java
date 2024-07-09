package org.hibernate.bugs;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

import org.hibernate.testing.bytecode.enhancement.BytecodeEnhancerRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(BytecodeEnhancerRunner.class)
public class LazyOneToOneBeTest {
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
				.addAnnotatedClass( Ship.class )
				.addAnnotatedClass( Captain.class )
				.buildMetadata();

		sf = metadata.buildSessionFactory();
	}

	@Test
	public void hhh123Test() {
		Captain robert = new Captain( "Robert Witterel" );
		Ship obraDinn = new Ship( "Obra Dinn" );
		obraDinn.setCaptain( robert );
		robert.setShip( obraDinn );

		try (Session session = sf.openSession()) {
			session.beginTransaction();
			session.persist( obraDinn );
			session.getTransaction().commit();
		}
		try (Session session = sf.openSession()) {
			session.beginTransaction();
			Ship ship = session.find( Ship.class, obraDinn.getId() );
			session.remove( ship );
			session.getTransaction().commit();
		}
		try (Session session = sf.openSession()) {
			session.beginTransaction();
			Ship ship = session.find( Ship.class, obraDinn.getId() );
			assertThat( ship ).isNull();
			Captain captain = session.find( Captain.class, robert.getId() );
			assertThat( captain ).isNull();
			session.getTransaction().commit();
		}
	}
}



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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Test;

import org.assertj.core.api.Assertions;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using its built-in unit test framework.
 * Although ORMStandaloneTestCase is perfectly acceptable as a reproducer, usage of this class is much preferred.
 * Since we nearly always include a regression test with bug fixes, providing your reproducer using this method
 * simplifies the process.
 *
 * What's even better?  Fork hibernate-orm itself, add your test case directly to a module's unit tests, then
 * submit it as a PR!
 */
public class ORMUnitTestCase extends BaseCoreFunctionalTestCase {

	// Add your entities here.
	@Override
	protected Class[] getAnnotatedClasses() {
		return new Class[] { ReferenceType.class, AnalysisType.class };
	}

	@Override
	protected void configure(Configuration configuration) {
		super.configure( configuration );

		configuration.setProperty( AvailableSettings.SHOW_SQL, Boolean.TRUE.toString() );
		configuration.setProperty( AvailableSettings.FORMAT_SQL, Boolean.TRUE.toString() );
		//configuration.setProperty( AvailableSettings.GENERATE_STATISTICS, "true" );
	}

	// Add your tests, using standard JUnit.
	@Test
	public void hhh123Test() throws Exception {
		ReferenceType referenceType1 = new ReferenceType();
		referenceType1.setId( 55L );
		referenceType1.setName( "Reference 1" );

		ReferenceType referenceType2 = new ReferenceType();
		referenceType2.setId( 77L );
		referenceType2.setName( "Reference 2" );

		AnalysisType analysisType = new AnalysisType();
		analysisType.setName( "Analysis" );
		analysisType.getReferenceTypes().add( referenceType1 );
		analysisType.getReferenceTypes().add( referenceType2 );

		// BaseCoreFunctionalTestCase automatically creates the SessionFactory and provides the Session.
		try (Session s = openSession()) {
			Transaction tx = s.beginTransaction();
			s.persist( referenceType1 );
			s.persist( referenceType2 );
			s.persist( analysisType );
			tx.commit();
		}

		try (Session s = openSession()) {
			final AnalysisType result = s.find( AnalysisType.class, analysisType.getName() );
			Assertions.assertThat( result.getReferenceTypes() ).containsExactlyInAnyOrder( referenceType2, referenceType1 );
		}
	}

	@Entity
	public static class AnalysisType {
		@Id
		private String name;

		@OneToMany
		private List<ReferenceType> referenceTypes = new ArrayList<>();

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<ReferenceType> getReferenceTypes() {
			return referenceTypes;
		}

		public void setReferenceTypes(List<ReferenceType> referenceTypes) {
			this.referenceTypes = referenceTypes;
		}
	}

	@Entity
	public static class ReferenceType {
		@Id
		private Long id;
		private String name;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( !( o instanceof ReferenceType ) ) {
				return false;
			}
			ReferenceType that = (ReferenceType) o;
			return Objects.equals( name, that.name );
		}

		@Override
		public int hashCode() {
			return Objects.hash( name );
		}
	}
}

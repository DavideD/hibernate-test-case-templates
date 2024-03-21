/* Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.bugs.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.EnumSet;
import java.util.Objects;

import org.hibernate.annotations.ValueGenerationType;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;

public class CurrentUser {

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

	public static abstract class AbstractLoggedUserGenerator implements BeforeExecutionGenerator {
		@Override
		public Object generate(
				SharedSessionContractImplementor session,
				Object owner,
				Object currentValue,
				EventType eventType) {
			Objects.requireNonNull( session );
			String value = CurrentUser.INSTANCE.get();
			return value;
		}
	}

	public static class InsertLoggedUserGenerator extends AbstractLoggedUserGenerator {

		@Override
		public EnumSet<EventType> getEventTypes() {
			return EventTypeSets.INSERT_ONLY;
		}
	}

	public static class AlwaysLoggedUserGenerator extends AbstractLoggedUserGenerator {

		@Override
		public EnumSet<EventType> getEventTypes() {
			return EventTypeSets.ALL;
		}
	}

}

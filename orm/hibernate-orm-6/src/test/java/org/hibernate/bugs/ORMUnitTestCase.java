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

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.spi.SessionImplementor;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.hibernate.testing.orm.junit.Setting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.assertj.core.api.Assertions;

import static org.assertj.core.api.Assertions.assertThat;

@DomainModel(annotatedClasses = QueryEntity.class)
@ServiceRegistry(settings = {
        @Setting(name = AvailableSettings.SHOW_SQL, value = "true"),
        @Setting(name = AvailableSettings.FORMAT_SQL, value = "false"),
        @Setting(name = AvailableSettings.HIGHLIGHT_SQL, value = "true")
})
@SessionFactory
class ORMUnitTestCase {

    QueryEntity entity;

    @BeforeEach
    public void setup(SessionFactoryScope scope) {
        scope.inTransaction(session -> {
            entity = new QueryEntity();
            entity.setDependency(new QueryEntity());
            session.persist(entity);
        });
    }

    @Test
    void hhh123Test(SessionFactoryScope scope) throws Exception {
        scope.inTransaction(session -> {
            assertThat(count(session)).isEqualTo(2);
            session.remove(session.find(QueryEntity.class, entity.id));
            assertThat(count(session)).isEqualTo(1);
        });
    }

    private static long count(SessionImplementor session) {
        return session.createQuery("from QueryEntity", QueryEntity.class).getResultCount();
    }
}

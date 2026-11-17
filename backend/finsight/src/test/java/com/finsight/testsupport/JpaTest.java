package com.finsight.testsupport;

import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.lang.annotation.*;

/**
 * Shared setup for every JPA test: an in-memory H2 database (PostgreSQL mode) whose tables are
 * generated from the entities, with Flyway switched off. No Docker or local Postgres needed.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:finsight;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public @interface JpaTest {
}

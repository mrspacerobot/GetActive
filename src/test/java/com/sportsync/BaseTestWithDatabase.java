package com.sportsync;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseTestWithDatabase {

    // Properly declare the PostGIS image as compatible with PostgreSQL
    static DockerImageName postgisImage =
            DockerImageName.parse("postgis/postgis:15-3.3-alpine") // Select the correct version
                    .asCompatibleSubstituteFor("postgres");

    @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(postgisImage);

    // Dynamically configure properties for the Spring Boot test context
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @PersistenceContext protected EntityManager entityManager;
}

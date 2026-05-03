package com.flatshareteam.flatsharebackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * Bazowa klasa dla testów integracyjnych endpointów.
 * Uruchamia pełny kontekst Spring Boot + bazę PostgreSQL.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(ObjectMapper.class)
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeAll
    static void cleanDatabaseAll(@Autowired EntityManager entityManager,
                                 @Autowired TransactionTemplate transactionTemplate) {

        transactionTemplate.executeWithoutResult(status -> {
            List<String> tableNames = entityManager.getMetamodel().getEntities().stream()
                    .map(entityType -> {
                        Table table = entityType.getJavaType().getAnnotation(Table.class);
                        return table != null && !table.name().isEmpty()
                                ? table.name()
                                : entityType.getName().toLowerCase();
                    })
                    .toList();

            for (String table : tableNames) {
                entityManager.createNativeQuery("TRUNCATE TABLE " + table + " CASCADE").executeUpdate();
            }
        });
    }

    protected String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
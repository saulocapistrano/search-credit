package br.com.searchcredit.interfaces.controller;

import br.com.searchcredit.infrastructure.kafka.KafkaEventPublisher;
import br.com.searchcredit.infrastructure.storage.MinioStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SuppressWarnings("resource")
@SpringBootTest(properties = {
        "spring.liquibase.enabled=true",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.show-sql=false",
        "spring.kafka.bootstrap-servers=localhost:9092"
})
@AutoConfigureMockMvc
class CreditoNumeroEndpointsIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MinioStorageService minioStorageService;

    @MockBean
    private KafkaEventPublisher kafkaEventPublisher;

    @Test
    void nextNumeroCreditoShouldReturnNumericAndIncrease() throws Exception {
        String first = mockMvc.perform(get("/api/creditos/next-numero-credito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", matchesPattern("^[0-9]+$")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String second = mockMvc.perform(get("/api/creditos/next-numero-credito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", matchesPattern("^[0-9]+$")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        org.assertj.core.api.Assertions.assertThat(second).isNotEqualTo(first);
    }

    @Test
    void nextNumeroNfseShouldReturnNumericAndIncrease() throws Exception {
        String first = mockMvc.perform(get("/api/creditos/next-numero-nfse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", matchesPattern("^[0-9]+$")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String second = mockMvc.perform(get("/api/creditos/next-numero-nfse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", matchesPattern("^[0-9]+$")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        org.assertj.core.api.Assertions.assertThat(second).isNotEqualTo(first);
    }
}

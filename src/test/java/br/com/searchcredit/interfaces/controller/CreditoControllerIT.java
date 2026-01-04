package br.com.searchcredit.interfaces.controller;

import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.enums.StatusCredito;
import br.com.searchcredit.infrastructure.repository.jpa.CreditoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.kafka.bootstrap-servers=localhost:9092"
})
@AutoConfigureMockMvc
class CreditoControllerIT {

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

    @Autowired
    private CreditoJpaRepository creditoJpaRepository;

    @BeforeEach
    void setUp() {
        creditoJpaRepository.deleteAll();

        creditoJpaRepository.save(Credito.builder()
                .numeroCredito("CRED-001")
                .numeroNfse("NFSE-001")
                .dataConstituicao(LocalDate.of(2024, 1, 10))
                .valorIssqn(new BigDecimal("10.00"))
                .valorFaturado(new BigDecimal("100.00"))
                .simplesNacional(true)
                .status(StatusCredito.EM_ANALISE)
                .dataSolicitacao(LocalDateTime.of(2024, 1, 11, 10, 0))
                .dataAnalise(null)
                .build());

        creditoJpaRepository.save(Credito.builder()
                .numeroCredito("CRED-002")
                .numeroNfse("NFSE-002")
                .dataConstituicao(LocalDate.of(2024, 2, 10))
                .valorIssqn(new BigDecimal("20.00"))
                .valorFaturado(new BigDecimal("200.00"))
                .simplesNacional(false)
                .status(StatusCredito.APROVADO)
                .dataSolicitacao(LocalDateTime.of(2024, 2, 11, 10, 0))
                .dataAnalise(LocalDateTime.of(2024, 2, 12, 10, 0))
                .build());

        creditoJpaRepository.save(Credito.builder()
                .numeroCredito("CRED-003")
                .numeroNfse("NFSE-003")
                .dataConstituicao(LocalDate.of(2024, 3, 10))
                .valorIssqn(new BigDecimal("30.00"))
                .valorFaturado(new BigDecimal("300.00"))
                .simplesNacional(true)
                .status(StatusCredito.REPROVADO)
                .dataSolicitacao(LocalDateTime.of(2024, 3, 11, 10, 0))
                .dataAnalise(LocalDateTime.of(2024, 3, 12, 10, 0))
                .build());
    }

    @Test
    void shouldReturnPagedListWithCorrectTotalElementsAndOrdering() throws Exception {
        mockMvc.perform(get("/api/creditos")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sortBy", "dataConstituicao")
                        .param("sortDir", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].numeroCredito").value("CRED-003"))
                .andExpect(jsonPath("$.content[0].dataConstituicao").value("2024-03-10"))
                .andExpect(jsonPath("$.content[1].numeroCredito").value("CRED-002"));

        mockMvc.perform(get("/api/creditos")
                        .param("page", "1")
                        .param("size", "2")
                        .param("sortBy", "dataConstituicao")
                        .param("sortDir", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].numeroCredito").value("CRED-001"));
    }
}

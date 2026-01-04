package br.com.searchcredit.interfaces.controller;

import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.enums.StatusCredito;
import br.com.searchcredit.infrastructure.repository.jpa.CreditoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SuppressWarnings("resource")
@SpringBootTest(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.kafka.bootstrap-servers=localhost:9092"
})
@AutoConfigureMockMvc
class CreditoAnaliseIT {

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

    private Credito emAnalise;
    private Credito aprovado;

    @BeforeEach
    void setUp() {
        creditoJpaRepository.deleteAll();

        emAnalise = creditoJpaRepository.save(Credito.builder()
                .numeroCredito("900100")
                .numeroNfse("1000100")
                .dataConstituicao(LocalDate.of(2024, 3, 15))
                .valorIssqn(new BigDecimal("10.00"))
                .valorFaturado(new BigDecimal("100.00"))
                .simplesNacional(true)
                .status(StatusCredito.EM_ANALISE)
                .dataSolicitacao(LocalDateTime.of(2024, 3, 15, 10, 0))
                .build());

        aprovado = creditoJpaRepository.save(Credito.builder()
                .numeroCredito("900101")
                .numeroNfse("1000101")
                .dataConstituicao(LocalDate.of(2024, 3, 14))
                .valorIssqn(new BigDecimal("10.00"))
                .valorFaturado(new BigDecimal("100.00"))
                .simplesNacional(true)
                .status(StatusCredito.APROVADO)
                .dataSolicitacao(LocalDateTime.of(2024, 3, 14, 10, 0))
                .dataAnalise(LocalDateTime.of(2024, 3, 14, 12, 0))
                .aprovadoPor("ADMIN_FULL")
                .build());
    }

    @Test
    void shouldReturn204AndUpdateFieldsWhenCreditoIsEmAnalise() throws Exception {
        String body = "{" +
                "\"status\":\"APROVADO\"," +
                "\"aprovadoPor\":\"ADMIN_FULL\"," +
                "\"comentarioAnalise\":\"OK\"" +
                "}";

        mockMvc.perform(put("/api/creditos/{id}/analise", emAnalise.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        Credito updated = creditoJpaRepository.findById(emAnalise.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(StatusCredito.APROVADO);
        assertThat(updated.getAprovadoPor()).isEqualTo("ADMIN_FULL");
        assertThat(updated.getComentarioAnalise()).isEqualTo("OK");
        assertThat(updated.getDataAnalise()).isNotNull();
    }

    @Test
    void shouldReturn400WhenNewStatusIsInvalid() throws Exception {
        String body = "{" +
                "\"status\":\"EM_ANALISE\"," +
                "\"aprovadoPor\":\"ADMIN_FULL\"" +
                "}";

        mockMvc.perform(put("/api/creditos/{id}/analise", emAnalise.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenCreditoIsNotEmAnalise() throws Exception {
        String body = "{" +
                "\"status\":\"REPROVADO\"," +
                "\"aprovadoPor\":\"ADMIN_FULL\"" +
                "}";

        mockMvc.perform(put("/api/creditos/{id}/analise", aprovado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenCreditoDoesNotExist() throws Exception {
        String body = "{" +
                "\"status\":\"APROVADO\"," +
                "\"aprovadoPor\":\"ADMIN_FULL\"" +
                "}";

        mockMvc.perform(put("/api/creditos/{id}/analise", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenAprovadoPorIsMissing() throws Exception {
        String body = "{" +
                "\"status\":\"APROVADO\"" +
                "}";

        mockMvc.perform(put("/api/creditos/{id}/analise", emAnalise.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}

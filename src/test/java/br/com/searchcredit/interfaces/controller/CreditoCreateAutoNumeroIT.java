package br.com.searchcredit.interfaces.controller;

import br.com.searchcredit.infrastructure.kafka.KafkaEventPublisher;
import br.com.searchcredit.infrastructure.storage.MinioStorageService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
class CreditoCreateAutoNumeroIT {

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
    void postCreditoWithoutNumerosShouldPersistAndReturnGeneratedNumeros() throws Exception {
        when(minioStorageService.uploadComprovante(ArgumentMatchers.any())).thenReturn("http://minio.local/comprovantes-renda/x.pdf");

        String creditoJson = "{" +
                "\"numeroCredito\":null," +
                "\"numeroNfse\":null," +
                "\"dataConstituicao\":\"2024-03-15\"," +
                "\"valorIssqn\":666.66," +
                "\"tipoCredito\":\"ISSQN\"," +
                "\"simplesNacional\":true," +
                "\"aliquota\":3.0," +
                "\"valorFaturado\":32000," +
                "\"valorDeducao\":2000," +
                "\"baseCalculo\":30000," +
                "\"solicitadoPor\":\"ADMIN_CREDITO\"" +
                "}";

        MockMultipartFile creditoPart = new MockMultipartFile(
                "credito",
                "credito.json",
                MediaType.APPLICATION_JSON_VALUE,
                creditoJson.getBytes()
        );

        MockMultipartFile comprovantePart = new MockMultipartFile(
                "comprovante",
                "comprovante.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "dummy".getBytes()
        );

        mockMvc.perform(multipart("/api/creditos")
                        .file(creditoPart)
                        .file(comprovantePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCredito", matchesPattern("^[0-9]+$")))
                .andExpect(jsonPath("$.numeroNfse", matchesPattern("^[0-9]+$")));
    }
}

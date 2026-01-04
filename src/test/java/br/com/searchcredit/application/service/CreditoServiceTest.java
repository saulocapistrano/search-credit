package br.com.searchcredit.application.service;

import br.com.searchcredit.application.dto.credito.CreditoQueryResponseDto;
import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.repository.CreditoRepository;
import br.com.searchcredit.infrastructure.kafka.KafkaEventPublisher;
import br.com.searchcredit.infrastructure.kafka.event.ConsultaCreditoEvent;
import br.com.searchcredit.infrastructure.storage.MinioStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreditoService - Testes Unitários")
class CreditoServiceTest {

    @Mock
    private CreditoRepository repository;

    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    @Mock
    private MinioStorageService minioStorageService;

    @Mock
    private CreditoNumeroGeneratorService creditoNumeroGeneratorService;

    @InjectMocks
    private CreditoService creditoService;

    private Credito credito;
    private String numeroNfse;
    private String numeroCredito;

    @BeforeEach
    void setUp() {
        numeroNfse = "NFSE123456";
        numeroCredito = "CRED001";

        credito = Credito.builder()
                .id(1L)
                .numeroCredito(numeroCredito)
                .numeroNfse(numeroNfse)
                .dataConstituicao(LocalDate.of(2024, 1, 15))
                .valorIssqn(new BigDecimal("1500.00"))
                .tipoCredito("ISS")
                .simplesNacional(true)
                .aliquota(new BigDecimal("5.0"))
                .valorFaturado(new BigDecimal("30000.00"))
                .valorDeducao(new BigDecimal("5000.00"))
                .baseCalculo(new BigDecimal("25000.00"))
                .build();
    }

    @Test
    @DisplayName("Deve retornar lista de créditos ao buscar por número da NFS-e existente")
    void shouldReturnListOfCreditosWhenNfseExists() {
        // Arrange
        List<Credito> creditos = List.of(credito);
        when(repository.findAllByNumeroNfse(numeroNfse)).thenReturn(creditos);

        // Act
        List<CreditoQueryResponseDto> result = creditoService.findAllByNumeroNfse(numeroNfse);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNumeroCredito()).isEqualTo(numeroCredito);
        assertThat(result.get(0).getNumeroNfse()).isEqualTo(numeroNfse);
        assertThat(result.get(0).getSimplesNacional()).isEqualTo("Sim");
        assertThat(result.get(0).getValorIssqn()).isEqualByComparingTo(new BigDecimal("1500.00"));

        verify(repository, times(1)).findAllByNumeroNfse(numeroNfse);
        verify(kafkaEventPublisher, times(1)).publishConsultaCredito(any(ConsultaCreditoEvent.class));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando NFS-e não existir")
    void shouldReturnEmptyListWhenNfseDoesNotExist() {
        // Arrange
        when(repository.findAllByNumeroNfse(numeroNfse)).thenReturn(Collections.emptyList());

        // Act
        List<CreditoQueryResponseDto> result = creditoService.findAllByNumeroNfse(numeroNfse);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(repository, times(1)).findAllByNumeroNfse(numeroNfse);
        verify(kafkaEventPublisher, times(1)).publishConsultaCredito(any(ConsultaCreditoEvent.class));
    }

    @Test
    @DisplayName("Deve retornar Optional com crédito quando número de crédito existir")
    void shouldReturnOptionalWithCreditoWhenNumeroCreditoExists() {
        // Arrange
        when(repository.findByNumeroCredito(numeroCredito)).thenReturn(Optional.of(credito));

        // Act
        Optional<CreditoQueryResponseDto> result = creditoService.findByNumeroCredito(numeroCredito);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getNumeroCredito()).isEqualTo(numeroCredito);
        assertThat(result.get().getNumeroNfse()).isEqualTo(numeroNfse);
        assertThat(result.get().getSimplesNacional()).isEqualTo("Sim");

        verify(repository, times(1)).findByNumeroCredito(numeroCredito);
        verify(kafkaEventPublisher, times(1)).publishConsultaCredito(any(ConsultaCreditoEvent.class));
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando número de crédito não existir")
    void shouldReturnEmptyOptionalWhenNumeroCreditoDoesNotExist() {
        // Arrange
        when(repository.findByNumeroCredito(numeroCredito)).thenReturn(Optional.empty());

        // Act
        Optional<CreditoQueryResponseDto> result = creditoService.findByNumeroCredito(numeroCredito);

        // Assert
        assertThat(result).isEmpty();

        verify(repository, times(1)).findByNumeroCredito(numeroCredito);
        verify(kafkaEventPublisher, times(1)).publishConsultaCredito(any(ConsultaCreditoEvent.class));
    }

    @Test
    @DisplayName("Deve publicar evento Kafka ao realizar consulta por NFS-e")
    void shouldPublishKafkaEventWhenConsultingByNfse() {
        // Arrange
        when(repository.findAllByNumeroNfse(numeroNfse)).thenReturn(List.of(credito));
        ArgumentCaptor<ConsultaCreditoEvent> eventCaptor = ArgumentCaptor.forClass(ConsultaCreditoEvent.class);

        // Act
        creditoService.findAllByNumeroNfse(numeroNfse);

        // Assert
        verify(kafkaEventPublisher, times(1)).publishConsultaCredito(eventCaptor.capture());
        ConsultaCreditoEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getTipoConsulta()).isEqualTo("numeroNfse");
        assertThat(capturedEvent.getValorConsulta()).isEqualTo(numeroNfse);
        assertThat(capturedEvent.getDataHora()).isNotNull();
    }

    @Test
    @DisplayName("Deve publicar evento Kafka ao realizar consulta por número de crédito")
    void shouldPublishKafkaEventWhenConsultingByNumeroCredito() {
        // Arrange
        when(repository.findByNumeroCredito(numeroCredito)).thenReturn(Optional.of(credito));
        ArgumentCaptor<ConsultaCreditoEvent> eventCaptor = ArgumentCaptor.forClass(ConsultaCreditoEvent.class);

        // Act
        creditoService.findByNumeroCredito(numeroCredito);

        // Assert
        verify(kafkaEventPublisher, times(1)).publishConsultaCredito(eventCaptor.capture());
        ConsultaCreditoEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getTipoConsulta()).isEqualTo("numeroCredito");
        assertThat(capturedEvent.getValorConsulta()).isEqualTo(numeroCredito);
        assertThat(capturedEvent.getDataHora()).isNotNull();
    }

    @Test
    @DisplayName("Deve converter corretamente simples nacional para 'Sim' quando true")
    void shouldConvertSimplesNacionalToSimWhenTrue() {
        // Arrange
        credito.setSimplesNacional(true);
        when(repository.findByNumeroCredito(numeroCredito)).thenReturn(Optional.of(credito));

        // Act
        Optional<CreditoQueryResponseDto> result = creditoService.findByNumeroCredito(numeroCredito);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getSimplesNacional()).isEqualTo("Sim");
    }

    @Test
    @DisplayName("Deve converter corretamente simples nacional para false quando false")
    void shouldConvertSimplesNacionalToFalseWhenFalse() {
        // Arrange
        credito.setSimplesNacional(false);
        when(repository.findByNumeroCredito(numeroCredito)).thenReturn(Optional.of(credito));

        // Act
        Optional<CreditoQueryResponseDto> result = creditoService.findByNumeroCredito(numeroCredito);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getSimplesNacional()).isEqualTo("Não");
    }

    @Test
    @DisplayName("Deve lançar exceção quando o repositório lançar exceção ao buscar por NFS-e")
    void shouldThrowExceptionWhenRepositoryThrowsExceptionOnFindByNfse() {
        // Arrange
        RuntimeException repositoryException = new RuntimeException("Erro ao acessar banco de dados");
        when(repository.findAllByNumeroNfse(numeroNfse)).thenThrow(repositoryException);

        // Act & Assert
        assertThatThrownBy(() -> creditoService.findAllByNumeroNfse(numeroNfse))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Erro ao acessar banco de dados");

        verify(repository, times(1)).findAllByNumeroNfse(numeroNfse);
        verify(kafkaEventPublisher, never()).publishConsultaCredito(any(ConsultaCreditoEvent.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o repositório lançar exceção ao buscar por número de crédito")
    void shouldThrowExceptionWhenRepositoryThrowsExceptionOnFindByNumeroCredito() {
        // Arrange
        RuntimeException repositoryException = new RuntimeException("Erro ao acessar banco de dados");
        when(repository.findByNumeroCredito(numeroCredito)).thenThrow(repositoryException);

        // Act & Assert
        assertThatThrownBy(() -> creditoService.findByNumeroCredito(numeroCredito))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Erro ao acessar banco de dados");

        verify(repository, times(1)).findByNumeroCredito(numeroCredito);
        verify(kafkaEventPublisher, never()).publishConsultaCredito(any(ConsultaCreditoEvent.class));
    }

    @Test
    @DisplayName("Deve retornar múltiplos créditos quando NFS-e tiver mais de um crédito associado")
    void shouldReturnMultipleCreditosWhenNfseHasMultipleCredits() {
        // Arrange
        Credito credito2 = Credito.builder()
                .id(2L)
                .numeroCredito("CRED002")
                .numeroNfse(numeroNfse)
                .dataConstituicao(LocalDate.of(2024, 1, 20))
                .valorIssqn(new BigDecimal("2000.00"))
                .tipoCredito("ISS")
                .simplesNacional(false)
                .aliquota(new BigDecimal("6.0"))
                .valorFaturado(new BigDecimal("40000.00"))
                .valorDeducao(new BigDecimal("6000.00"))
                .baseCalculo(new BigDecimal("34000.00"))
                .build();

        List<Credito> creditos = List.of(credito, credito2);
        when(repository.findAllByNumeroNfse(numeroNfse)).thenReturn(creditos);

        // Act
        List<CreditoQueryResponseDto> result = creditoService.findAllByNumeroNfse(numeroNfse);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNumeroCredito()).isEqualTo("CRED001");
        assertThat(result.get(1).getNumeroCredito()).isEqualTo("CRED002");

        verify(repository, times(1)).findAllByNumeroNfse(numeroNfse);
        verify(kafkaEventPublisher, times(1)).publishConsultaCredito(any(ConsultaCreditoEvent.class));
    }
}


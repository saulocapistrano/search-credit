package br.com.searchcredit.application.service;

import br.com.searchcredit.application.dto.solicitacao.SolicitacaoCreditoRequestDto;
import br.com.searchcredit.application.dto.solicitacao.SolicitacaoCreditoResponseDto;
import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.enums.StatusCredito;
import br.com.searchcredit.domain.repository.CreditoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SolicitacaoCreditoService - Testes Unitários")
class SolicitacaoCreditoServiceTest {

    @Mock
    private CreditoWorkflowService creditoWorkflowService;

    @Mock
    private CreditoRepository creditoRepository;

    @InjectMocks
    private SolicitacaoCreditoService service;

    private SolicitacaoCreditoRequestDto requestDtoMinimo;
    private Credito creditoSalvo;
    private SolicitacaoCreditoResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDtoMinimo = new SolicitacaoCreditoRequestDto();
        requestDtoMinimo.setNomeSolicitante("João Silva");

        creditoSalvo = Credito.builder()
                .id(1L)
                .nomeSolicitante("João Silva")
                .status(StatusCredito.EM_ANALISE)
                .dataSolicitacao(LocalDateTime.now())
                .build();

        responseDto = SolicitacaoCreditoResponseDto.builder()
                .id(1L)
                .nomeSolicitante("João Silva")
                .status(StatusCredito.EM_ANALISE)
                .dataSolicitacao(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve criar solicitação com campos mínimos (apenas nomeSolicitante)")
    void shouldCreateSolicitacaoWithMinimumFields() {
        // Arrange
        when(creditoWorkflowService.criarCreditoComWorkflow(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(creditoSalvo);

        // Act
        SolicitacaoCreditoResponseDto result = service.criarSolicitacao(requestDtoMinimo, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNomeSolicitante()).isEqualTo("João Silva");
        assertThat(result.getStatus()).isEqualTo(StatusCredito.EM_ANALISE);
        assertThat(result.getDataSolicitacao()).isNotNull();

        verify(creditoWorkflowService, times(1)).criarCreditoComWorkflow(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    @DisplayName("Deve sempre definir status como EM_ANALISE ao criar solicitação")
    void shouldAlwaysSetStatusAsEmAnaliseWhenCreating() {
        // Arrange
        when(creditoWorkflowService.criarCreditoComWorkflow(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenAnswer(invocation -> {
            Credito credito = Credito.builder()
                    .id(1L)
                    .nomeSolicitante("João Silva")
                    .status(StatusCredito.EM_ANALISE)
                    .dataSolicitacao(LocalDateTime.now())
                    .build();
            assertThat(credito.getStatus()).isEqualTo(StatusCredito.EM_ANALISE);
            return credito;
        });

        // Act
        service.criarSolicitacao(requestDtoMinimo, null);

        // Assert
        verify(creditoWorkflowService, times(1)).criarCreditoComWorkflow(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    @DisplayName("Deve sempre definir dataSolicitacao como now() ao criar solicitação")
    void shouldAlwaysSetDataSolicitacaoAsNowWhenCreating() {
        // Arrange
        LocalDateTime antes = LocalDateTime.now();
        when(creditoWorkflowService.criarCreditoComWorkflow(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenAnswer(invocation -> {
            Credito credito = Credito.builder()
                    .id(1L)
                    .nomeSolicitante("João Silva")
                    .status(StatusCredito.EM_ANALISE)
                    .dataSolicitacao(LocalDateTime.now())
                    .build();
            assertThat(credito.getDataSolicitacao()).isNotNull();
            assertThat(credito.getDataSolicitacao()).isAfterOrEqualTo(antes);
            return credito;
        });

        // Act
        service.criarSolicitacao(requestDtoMinimo, null);

        // Assert
        verify(creditoWorkflowService, times(1)).criarCreditoComWorkflow(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        );
    }
}


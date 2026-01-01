package br.com.searchcredit.application.service;

import br.com.searchcredit.application.dto.solicitacao.SolicitacaoCreditoRequestDto;
import br.com.searchcredit.application.dto.solicitacao.SolicitacaoCreditoResponseDto;
import br.com.searchcredit.application.mapper.SolicitacaoCreditoMapper;
import br.com.searchcredit.domain.entity.SolicitacaoCredito;
import br.com.searchcredit.domain.enums.StatusSolicitacao;
import br.com.searchcredit.domain.repository.SolicitacaoCreditoRepository;
import br.com.searchcredit.infrastructure.repository.jpa.SolicitacaoCreditoJpaRepository;
import br.com.searchcredit.infrastructure.storage.MinioStorageService;
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
    private SolicitacaoCreditoRepository repository;

    @Mock
    private SolicitacaoCreditoJpaRepository jpaRepository;

    @Mock
    private MinioStorageService minioStorageService;

    @Mock
    private SolicitacaoCreditoMapper mapper;

    @InjectMocks
    private SolicitacaoCreditoService service;

    private SolicitacaoCreditoRequestDto requestDtoMinimo;
    private SolicitacaoCredito solicitacaoSalva;
    private SolicitacaoCreditoResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDtoMinimo = new SolicitacaoCreditoRequestDto();
        requestDtoMinimo.setNomeSolicitante("João Silva");

        solicitacaoSalva = SolicitacaoCredito.builder()
                .id(1L)
                .nomeSolicitante("João Silva")
                .status(StatusSolicitacao.EM_ANALISE)
                .dataSolicitacao(LocalDateTime.now())
                .build();

        responseDto = SolicitacaoCreditoResponseDto.builder()
                .id(1L)
                .nomeSolicitante("João Silva")
                .status(StatusSolicitacao.EM_ANALISE)
                .dataSolicitacao(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve criar solicitação com campos mínimos (apenas nomeSolicitante)")
    void shouldCreateSolicitacaoWithMinimumFields() {
        // Arrange
        when(repository.save(any(SolicitacaoCredito.class))).thenReturn(solicitacaoSalva);
        when(mapper.toResponse(any(SolicitacaoCredito.class))).thenReturn(responseDto);

        // Act
        SolicitacaoCreditoResponseDto result = service.criarSolicitacao(requestDtoMinimo, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNomeSolicitante()).isEqualTo("João Silva");
        assertThat(result.getStatus()).isEqualTo(StatusSolicitacao.EM_ANALISE);
        assertThat(result.getDataSolicitacao()).isNotNull();

        verify(repository, times(1)).save(any(SolicitacaoCredito.class));
        verify(mapper, times(1)).toResponse(any(SolicitacaoCredito.class));
        verify(minioStorageService, never()).uploadComprovante(any());
    }

    @Test
    @DisplayName("Deve sempre definir status como EM_ANALISE ao criar solicitação")
    void shouldAlwaysSetStatusAsEmAnaliseWhenCreating() {
        // Arrange
        when(repository.save(any(SolicitacaoCredito.class))).thenAnswer(invocation -> {
            SolicitacaoCredito solicitacao = invocation.getArgument(0);
            assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.EM_ANALISE);
            return solicitacaoSalva;
        });
        when(mapper.toResponse(any(SolicitacaoCredito.class))).thenReturn(responseDto);

        // Act
        service.criarSolicitacao(requestDtoMinimo, null);

        // Assert
        verify(repository, times(1)).save(any(SolicitacaoCredito.class));
    }

    @Test
    @DisplayName("Deve sempre definir dataSolicitacao como now() ao criar solicitação")
    void shouldAlwaysSetDataSolicitacaoAsNowWhenCreating() {
        // Arrange
        LocalDateTime antes = LocalDateTime.now();
        when(repository.save(any(SolicitacaoCredito.class))).thenAnswer(invocation -> {
            SolicitacaoCredito solicitacao = invocation.getArgument(0);
            assertThat(solicitacao.getDataSolicitacao()).isNotNull();
            assertThat(solicitacao.getDataSolicitacao()).isAfterOrEqualTo(antes);
            return solicitacaoSalva;
        });
        when(mapper.toResponse(any(SolicitacaoCredito.class))).thenReturn(responseDto);

        // Act
        service.criarSolicitacao(requestDtoMinimo, null);

        // Assert
        verify(repository, times(1)).save(any(SolicitacaoCredito.class));
    }
}


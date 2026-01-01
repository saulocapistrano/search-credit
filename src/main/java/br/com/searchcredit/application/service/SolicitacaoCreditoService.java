package br.com.searchcredit.application.service;

import br.com.searchcredit.application.dto.solicitacao.AtualizarStatusRequestDto;
import br.com.searchcredit.application.dto.solicitacao.SolicitacaoCreditoRequestDto;
import br.com.searchcredit.application.dto.solicitacao.SolicitacaoCreditoResponseDto;
import br.com.searchcredit.application.mapper.SolicitacaoCreditoMapper;
import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.entity.SolicitacaoCredito;
import br.com.searchcredit.domain.enums.StatusCredito;
import br.com.searchcredit.domain.enums.StatusSolicitacao;
import br.com.searchcredit.domain.repository.SolicitacaoCreditoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitacaoCreditoService {

    private final SolicitacaoCreditoRepository repository;
    private final SolicitacaoCreditoMapper mapper;
    private final CreditoWorkflowService creditoWorkflowService;

    @Transactional
    public SolicitacaoCreditoResponseDto criarSolicitacao(
            SolicitacaoCreditoRequestDto requestDto,
            MultipartFile comprovante) {

        // Delegar criação para CreditoWorkflowService (cria diretamente em Credito)
        Credito creditoCriado = creditoWorkflowService.criarCreditoComWorkflow(
                requestDto.getNumeroCredito(),
                requestDto.getNumeroNfse(),
                requestDto.getDataConstituicao(),
                requestDto.getValorIssqn(),
                requestDto.getTipoCredito(),
                requestDto.getSimplesNacional(),
                requestDto.getAliquota(),
                requestDto.getValorFaturado(),
                requestDto.getValorDeducao(),
                requestDto.getBaseCalculo(),
                requestDto.getNomeSolicitante(),
                comprovante
        );

        // Converter Credito para SolicitacaoCreditoResponseDto
        return toResponseDto(creditoCriado);
    }

    /**
     * Converte Credito para SolicitacaoCreditoResponseDto.
     * Converte StatusCredito para StatusSolicitacao para manter compatibilidade.
     */
    private SolicitacaoCreditoResponseDto toResponseDto(Credito credito) {
        if (credito == null) {
            return null;
        }

        // Converter StatusCredito para StatusSolicitacao
        StatusSolicitacao statusSolicitacao = convertStatusCreditoToStatusSolicitacao(credito.getStatus());

        return SolicitacaoCreditoResponseDto.builder()
                .id(credito.getId())
                .numeroCredito(credito.getNumeroCredito())
                .numeroNfse(credito.getNumeroNfse())
                .dataConstituicao(credito.getDataConstituicao())
                .valorIssqn(credito.getValorIssqn())
                .tipoCredito(credito.getTipoCredito())
                .simplesNacional(credito.isSimplesNacional() ? "Sim" : "Não")
                .aliquota(credito.getAliquota())
                .valorFaturado(credito.getValorFaturado())
                .valorDeducao(credito.getValorDeducao())
                .baseCalculo(credito.getBaseCalculo())
                .status(statusSolicitacao)
                .nomeSolicitante(credito.getNomeSolicitante())
                .comprovanteUrl(credito.getComprovanteUrl())
                .dataSolicitacao(credito.getDataSolicitacao())
                .comentarioAnalise(credito.getComentarioAnalise())
                .dataAnalise(credito.getDataAnalise())
                .build();
    }

    /**
     * Converte StatusCredito para StatusSolicitacao.
     * StatusCredito.CONSTITUIDO não existe em StatusSolicitacao, então retorna null ou EM_ANALISE.
     */
    private StatusSolicitacao convertStatusCreditoToStatusSolicitacao(StatusCredito statusCredito) {
        if (statusCredito == null) {
            return null;
        }

        return switch (statusCredito) {
            case EM_ANALISE -> StatusSolicitacao.EM_ANALISE;
            case APROVADO -> StatusSolicitacao.APROVADO;
            case REPROVADO -> StatusSolicitacao.REPROVADO;
            case CONSTITUIDO -> StatusSolicitacao.EM_ANALISE; // Fallback para CONSTITUIDO
        };
    }

    public List<SolicitacaoCreditoResponseDto> listarTodas() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<SolicitacaoCreditoResponseDto> buscarTodas() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<SolicitacaoCreditoResponseDto> listarPorSolicitante(String nomeSolicitante) {
        return repository.findByNomeSolicitante(nomeSolicitante).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public Page<SolicitacaoCreditoResponseDto> listarPorSolicitante(String nomeSolicitante, int page, int size) {
        Page<SolicitacaoCredito> solicitacoes = repository.findByNomeSolicitante(nomeSolicitante, page, size);
        return solicitacoes.map(mapper::toResponse);
    }

    public Page<SolicitacaoCreditoResponseDto> listarTodas(int page, int size, String sortBy, String sortDir) {
        Page<SolicitacaoCredito> solicitacoes = repository.findAll(page, size, sortBy, sortDir);
        return solicitacoes.map(mapper::toResponse);
    }

    public String gerarProximoNumeroCredito() {
        List<SolicitacaoCredito> solicitacoes = repository.findAllOrderByNumeroCreditoDesc();
        if (solicitacoes.isEmpty()) {
            return "CRED000001";
        }
        
        String ultimoNumero = solicitacoes.get(0).getNumeroCredito();
        if (ultimoNumero != null && ultimoNumero.startsWith("CRED")) {
            try {
                String numeroStr = ultimoNumero.substring(4);
                long numero = Long.parseLong(numeroStr);
                return String.format("CRED%06d", numero + 1);
            } catch (NumberFormatException e) {
                return "CRED000001";
            }
        }
        return "CRED000001";
    }

    public String gerarProximoNumeroNfse() {
        List<SolicitacaoCredito> solicitacoes = repository.findAllOrderByNumeroNfseDesc();
        if (solicitacoes.isEmpty()) {
            return "NFSE1000001";
        }
        
        String ultimoNumero = solicitacoes.get(0).getNumeroNfse();
        if (ultimoNumero != null && ultimoNumero.startsWith("NFSE")) {
            try {
                String numeroStr = ultimoNumero.substring(4);
                long numero = Long.parseLong(numeroStr);
                return String.format("NFSE%07d", numero + 1);
            } catch (NumberFormatException e) {
                return "NFSE1000001";
            }
        }
        return "NFSE1000001";
    }

    public List<SolicitacaoCreditoResponseDto> listarPorNumeroCredito(String numeroCredito) {
        return repository.findByNumeroCredito(numeroCredito).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<SolicitacaoCreditoResponseDto> listarPorNumeroNfse(String numeroNfse) {
        return repository.findByNumeroNfse(numeroNfse).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public Optional<SolicitacaoCreditoResponseDto> buscarPorId(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse);
    }

    @Transactional
    public Optional<SolicitacaoCreditoResponseDto> atualizarStatus(Long id, AtualizarStatusRequestDto requestDto) {
        return repository.findById(id)
                .map(solicitacao -> {
                    // Bloquear mudança direta para APROVADO ou REPROVADO
                    // Esses status só podem ser definidos através do fluxo de análise (/analise)
                    if (requestDto.getStatus() == StatusSolicitacao.APROVADO || 
                        requestDto.getStatus() == StatusSolicitacao.REPROVADO) {
                        throw new IllegalStateException(
                                String.format(
                                        "Não é possível alterar o status diretamente para '%s'. " +
                                        "Use o endpoint /api/solicitacoes/%d/analise para realizar a análise da solicitação.",
                                        requestDto.getStatus(),
                                        id
                                )
                        );
                    }
                    solicitacao.setStatus(requestDto.getStatus());
                    SolicitacaoCredito updated = repository.save(solicitacao);
                    return mapper.toResponse(updated);
                });
    }
}


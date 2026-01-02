package br.com.searchcredit.application.service;

import br.com.searchcredit.application.dto.solicitacao.AtualizarStatusRequestDto;
import br.com.searchcredit.application.dto.solicitacao.SolicitacaoCreditoRequestDto;
import br.com.searchcredit.application.dto.solicitacao.SolicitacaoCreditoResponseDto;
import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.enums.StatusCredito;
import br.com.searchcredit.domain.repository.CreditoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private final CreditoWorkflowService creditoWorkflowService;
    private final CreditoRepository creditoRepository;

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
     */
    private SolicitacaoCreditoResponseDto toResponseDto(Credito credito) {
        if (credito == null) {
            return null;
        }

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
                .status(credito.getStatus())
                .nomeSolicitante(credito.getNomeSolicitante())
                .comprovanteUrl(credito.getComprovanteUrl())
                .dataSolicitacao(credito.getDataSolicitacao())
                .comentarioAnalise(credito.getComentarioAnalise())
                .dataAnalise(credito.getDataAnalise())
                .build();
    }

    public List<SolicitacaoCreditoResponseDto> listarTodas() {
        return creditoRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<SolicitacaoCreditoResponseDto> buscarTodas() {
        return creditoRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<SolicitacaoCreditoResponseDto> listarPorSolicitante(String nomeSolicitante) {
        return creditoRepository.findByNomeSolicitante(nomeSolicitante).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public Page<SolicitacaoCreditoResponseDto> listarPorSolicitante(String nomeSolicitante, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Credito> creditos = creditoRepository.findByNomeSolicitante(nomeSolicitante, pageable);
        return creditos.map(this::toResponseDto);
    }

    public Page<SolicitacaoCreditoResponseDto> listarTodas(int page, int size, String sortBy, String sortDir) {
        Page<Credito> creditos = creditoRepository.findAll(page, size, sortBy, sortDir);
        return creditos.map(this::toResponseDto);
    }

    public String gerarProximoNumeroCredito() {
        List<Credito> creditos = creditoRepository.findAllOrderByNumeroCreditoDesc();
        if (creditos.isEmpty()) {
            return "CRED000001";
        }
        
        String ultimoNumero = creditos.get(0).getNumeroCredito();
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
        List<Credito> creditos = creditoRepository.findAllOrderByNumeroNfseDesc();
        if (creditos.isEmpty()) {
            return "NFSE1000001";
        }
        
        String ultimoNumero = creditos.get(0).getNumeroNfse();
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
        return creditoRepository.findByNumeroCreditoList(numeroCredito).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<SolicitacaoCreditoResponseDto> listarPorNumeroNfse(String numeroNfse) {
        return creditoRepository.findByNumeroNfseList(numeroNfse).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public Optional<SolicitacaoCreditoResponseDto> buscarPorId(Long id) {
        return creditoRepository.findById(id)
                .map(this::toResponseDto);
    }

    @Transactional
    public Optional<SolicitacaoCreditoResponseDto> atualizarStatus(Long id, AtualizarStatusRequestDto requestDto) {
        return creditoRepository.findById(id)
                .map(credito -> {
                    // Bloquear mudança direta para APROVADO ou REPROVADO
                    // Esses status só podem ser definidos através do fluxo de análise (/analise)
                    if (requestDto.getStatus() == StatusCredito.APROVADO || 
                        requestDto.getStatus() == StatusCredito.REPROVADO) {
                        throw new IllegalStateException(
                                String.format(
                                        "Não é possível alterar o status diretamente para '%s'. " +
                                        "Use o endpoint /api/solicitacoes/%d/analise para realizar a análise da solicitação.",
                                        requestDto.getStatus(),
                                        id
                                )
                        );
                    }
                    
                    credito.setStatus(requestDto.getStatus());
                    Credito updated = creditoRepository.save(credito);
                    return toResponseDto(updated);
                });
    }
}


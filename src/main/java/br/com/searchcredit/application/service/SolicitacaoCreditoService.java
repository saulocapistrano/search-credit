package br.com.searchcredit.application.service;

import br.com.searchcredit.application.dto.solicitacao.AtualizarStatusRequestDto;
import br.com.searchcredit.application.dto.solicitacao.SolicitacaoCreditoRequestDto;
import br.com.searchcredit.application.dto.solicitacao.SolicitacaoCreditoResponseDto;
import br.com.searchcredit.domain.entity.SolicitacaoCredito;
import br.com.searchcredit.domain.enums.StatusSolicitacao;
import br.com.searchcredit.domain.repository.SolicitacaoCreditoRepository;
import br.com.searchcredit.infrastructure.storage.MinioStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitacaoCreditoService {

    private final SolicitacaoCreditoRepository repository;
    private final MinioStorageService minioStorageService;

    @Transactional
    public SolicitacaoCreditoResponseDto criarSolicitacao(
            SolicitacaoCreditoRequestDto requestDto,
            MultipartFile comprovante) {

        String comprovanteUrl = null;
        if (comprovante != null && !comprovante.isEmpty()) {
            comprovanteUrl = minioStorageService.uploadComprovante(comprovante);
        }

        SolicitacaoCredito solicitacao = SolicitacaoCredito.builder()
                .numeroCredito(requestDto.getNumeroCredito())
                .numeroNfse(requestDto.getNumeroNfse())
                .dataConstituicao(requestDto.getDataConstituicao())
                .valorIssqn(requestDto.getValorIssqn())
                .tipoCredito(requestDto.getTipoCredito())
                .simplesNacional(requestDto.getSimplesNacional())
                .aliquota(requestDto.getAliquota())
                .valorFaturado(requestDto.getValorFaturado())
                .valorDeducao(requestDto.getValorDeducao())
                .baseCalculo(requestDto.getBaseCalculo())
                .status(StatusSolicitacao.EM_ANALISE)
                .nomeSolicitante(requestDto.getNomeSolicitante())
                .comprovanteUrl(comprovanteUrl)
                .dataSolicitacao(LocalDateTime.now())
                .build();

        SolicitacaoCredito saved = repository.save(solicitacao);
        return toDto(saved);
    }

    public List<SolicitacaoCreditoResponseDto> listarTodas() {
        return repository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<SolicitacaoCreditoResponseDto> listarPorSolicitante(String nomeSolicitante) {
        return repository.findByNomeSolicitante(nomeSolicitante).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<SolicitacaoCreditoResponseDto> listarPorNumeroCredito(String numeroCredito) {
        return repository.findByNumeroCredito(numeroCredito).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Optional<SolicitacaoCreditoResponseDto> buscarPorId(Long id) {
        return repository.findById(id)
                .map(this::toDto);
    }

    @Transactional
    public Optional<SolicitacaoCreditoResponseDto> atualizarStatus(Long id, AtualizarStatusRequestDto requestDto) {
        return repository.findById(id)
                .map(solicitacao -> {
                    solicitacao.setStatus(requestDto.getStatus());
                    SolicitacaoCredito updated = repository.save(solicitacao);
                    return toDto(updated);
                });
    }

    private SolicitacaoCreditoResponseDto toDto(SolicitacaoCredito solicitacao) {
        return SolicitacaoCreditoResponseDto.builder()
                .id(solicitacao.getId())
                .numeroCredito(solicitacao.getNumeroCredito())
                .numeroNfse(solicitacao.getNumeroNfse())
                .dataConstituicao(solicitacao.getDataConstituicao())
                .valorIssqn(solicitacao.getValorIssqn())
                .tipoCredito(solicitacao.getTipoCredito())
                .simplesNacional(solicitacao.isSimplesNacional() ? "Sim" : "Não")
                .aliquota(solicitacao.getAliquota())
                .valorFaturado(solicitacao.getValorFaturado())
                .valorDeducao(solicitacao.getValorDeducao())
                .baseCalculo(solicitacao.getBaseCalculo())
                .status(solicitacao.getStatus())
                .nomeSolicitante(solicitacao.getNomeSolicitante())
                .comprovanteUrl(solicitacao.getComprovanteUrl())
                .dataSolicitacao(solicitacao.getDataSolicitacao())
                .build();
    }
}


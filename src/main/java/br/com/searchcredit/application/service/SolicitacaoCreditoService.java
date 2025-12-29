package br.com.searchcredit.application.service;

import br.com.searchcredit.application.dto.solicitacao.AtualizarStatusRequestDto;
import br.com.searchcredit.application.dto.solicitacao.SolicitacaoCreditoRequestDto;
import br.com.searchcredit.application.dto.solicitacao.SolicitacaoCreditoResponseDto;
import br.com.searchcredit.application.mapper.SolicitacaoCreditoMapper;
import br.com.searchcredit.domain.entity.SolicitacaoCredito;
import br.com.searchcredit.domain.enums.StatusSolicitacao;
import br.com.searchcredit.domain.repository.SolicitacaoCreditoRepository;
import br.com.searchcredit.infrastructure.repository.jpa.SolicitacaoCreditoJpaRepository;
import br.com.searchcredit.infrastructure.storage.MinioStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final SolicitacaoCreditoJpaRepository jpaRepository;
    private final MinioStorageService minioStorageService;
    private final SolicitacaoCreditoMapper mapper;

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
        return mapper.toResponse(saved);
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
        Pageable pageable = PageRequest.of(page, size);
        Page<SolicitacaoCredito> solicitacoes = jpaRepository.findByNomeSolicitante(nomeSolicitante, pageable);
        return solicitacoes.map(mapper::toResponse);
    }

    public Page<SolicitacaoCreditoResponseDto> listarTodas(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SolicitacaoCredito> solicitacoes = jpaRepository.findAll(pageable);
        return solicitacoes.map(mapper::toResponse);
    }

    public String gerarProximoNumeroCredito() {
        List<SolicitacaoCredito> solicitacoes = jpaRepository.findAllOrderByNumeroCreditoDesc();
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
        List<SolicitacaoCredito> solicitacoes = jpaRepository.findAllOrderByNumeroNfseDesc();
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
                    solicitacao.setStatus(requestDto.getStatus());
                    SolicitacaoCredito updated = repository.save(solicitacao);
                    return mapper.toResponse(updated);
                });
    }
}


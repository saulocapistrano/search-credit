package br.com.searchcredit.application.service;

import br.com.searchcredit.application.dto.credito.CreditoAdminResponseDto;
import br.com.searchcredit.application.dto.credito.CreditoAnaliseRequestDto;
import br.com.searchcredit.application.dto.credito.CreditoCreateRequestDto;
import br.com.searchcredit.application.dto.credito.CreditoQueryResponseDto;
import br.com.searchcredit.application.dto.credito.CreditoResponseDto;
import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.enums.StatusCredito;
import br.com.searchcredit.domain.repository.CreditoRepository;
import br.com.searchcredit.application.exception.CreditoAnaliseBadRequestException;
import br.com.searchcredit.application.exception.CreditoNotFoundException;
import br.com.searchcredit.infrastructure.kafka.KafkaEventPublisher;
import br.com.searchcredit.infrastructure.kafka.event.ConsultaCreditoEvent;
import br.com.searchcredit.infrastructure.kafka.event.SolicitacaoCreditoEvent;
import br.com.searchcredit.infrastructure.storage.MinioStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class CreditoService {

    private final CreditoRepository repository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final MinioStorageService minioStorageService;
    private final CreditoNumeroGeneratorService creditoNumeroGeneratorService;

    public CreditoService(
            CreditoRepository repository,
            KafkaEventPublisher kafkaEventPublisher,
            MinioStorageService minioStorageService,
            CreditoNumeroGeneratorService creditoNumeroGeneratorService) {
        this.repository = repository;
        this.kafkaEventPublisher = kafkaEventPublisher;
        this.minioStorageService = minioStorageService;
        this.creditoNumeroGeneratorService = creditoNumeroGeneratorService;
    }

    public Optional<CreditoQueryResponseDto> findByNumeroCredito(String numeroCredito){
        Optional<CreditoQueryResponseDto> result = repository.findByNumeroCredito(numeroCredito)
                .map(this::toQueryDto);
        try {
            kafkaEventPublisher.publishConsultaCredito(new ConsultaCreditoEvent("numeroCredito", numeroCredito));
        } catch (Exception e) {
            log.warn("Falha ao publicar evento Kafka para consulta por número de crédito: {}", numeroCredito, e);
        }
        return result;
    }

    public Page<CreditoResponseDto> listAll(int page, int size, String sortBy, String sortDir) {
        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findAll(pageable)
                .map(this::toListDto);
    }

    public List<CreditoQueryResponseDto> findAllByNumeroNfse(String numeroNfse){
        List<CreditoQueryResponseDto> result = repository.findAllByNumeroNfse(numeroNfse)
                .stream()
                .map(this::toQueryDto)
                .collect(Collectors.toList());
        try {
            kafkaEventPublisher.publishConsultaCredito(new ConsultaCreditoEvent("numeroNfse", numeroNfse));
        } catch (Exception e) {
            log.warn("Falha ao publicar evento Kafka para consulta por número NFS-e: {}", numeroNfse, e);
        }
        return result;
    }

    public CreditoAdminResponseDto create(CreditoCreateRequestDto requestDto) {
        return create(requestDto, null);
    }

    public CreditoAdminResponseDto create(CreditoCreateRequestDto requestDto, MultipartFile comprovante) {
        Credito credito = new Credito();

        String numeroCredito = requestDto.getNumeroCredito();
        if (numeroCredito == null || numeroCredito.isBlank()) {
            numeroCredito = creditoNumeroGeneratorService.nextNumeroCredito();
        }

        String numeroNfse = requestDto.getNumeroNfse();
        if (numeroNfse == null || numeroNfse.isBlank()) {
            numeroNfse = creditoNumeroGeneratorService.nextNumeroNfse();
        }

        credito.setNumeroCredito(numeroCredito);
        credito.setNumeroNfse(numeroNfse);
        credito.setDataConstituicao(requestDto.getDataConstituicao());
        credito.setValorIssqn(requestDto.getValorIssqn());
        credito.setTipoCredito(requestDto.getTipoCredito());
        credito.setSimplesNacional(requestDto.getSimplesNacional());
        credito.setAliquota(requestDto.getAliquota());
        credito.setValorFaturado(requestDto.getValorFaturado());
        credito.setValorDeducao(requestDto.getValorDeducao());
        credito.setBaseCalculo(requestDto.getBaseCalculo());

        credito.setStatus(StatusCredito.EM_ANALISE);
        credito.setDataSolicitacao(LocalDateTime.now());
        credito.setSolicitadoPor(requestDto.getSolicitadoPor());

        if (comprovante != null && !comprovante.isEmpty()) {
            String comprovanteUrl = minioStorageService.uploadComprovante(comprovante);
            credito.setComprovanteUrl(comprovanteUrl);
        }

        Credito saved = repository.save(credito);

        try {
            kafkaEventPublisher.publishSolicitacaoCredito(
                    new SolicitacaoCreditoEvent(
                            saved.getNumeroCredito(),
                            saved.getNumeroNfse(),
                            saved.getSolicitadoPor()
                    )
            );
        } catch (Exception e) {
            log.warn("Falha ao publicar evento Kafka de solicitação de crédito", e);
        }
        return toAdminDto(saved);
    }

    public void analisar(Long id, CreditoAnaliseRequestDto requestDto) {
        analisarTransacional(id, requestDto);
    }

    @Transactional
    protected void analisarTransacional(Long id, CreditoAnaliseRequestDto requestDto) {
        if (requestDto == null || requestDto.getStatus() == null) {
            throw new CreditoAnaliseBadRequestException("Status é obrigatório");
        }

        if (requestDto.getStatus() != StatusCredito.APROVADO && requestDto.getStatus() != StatusCredito.REPROVADO) {
            throw new CreditoAnaliseBadRequestException("Status inválido para análise");
        }

        if (requestDto.getAprovadoPor() == null || requestDto.getAprovadoPor().isBlank()) {
            throw new CreditoAnaliseBadRequestException("aprovadoPor é obrigatório");
        }

        Credito credito = repository.findById(id)
                .orElseThrow(() -> new CreditoNotFoundException("Crédito não encontrado"));

        if (credito.getStatus() != StatusCredito.EM_ANALISE) {
            throw new CreditoAnaliseBadRequestException("Crédito não está em análise");
        }

        credito.setStatus(requestDto.getStatus());
        credito.setAprovadoPor(requestDto.getAprovadoPor());
        credito.setComentarioAnalise(requestDto.getComentarioAnalise());
        credito.setDataAnalise(LocalDateTime.now());

        repository.save(credito);
    }

    private CreditoQueryResponseDto toQueryDto(Credito credito){
        String situacao = credito.getStatus() != null ? credito.getStatus().name() : null;
        return CreditoQueryResponseDto.builder()
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
                .situacao(situacao)
                .status(situacao)
                .build();
    }

    private CreditoResponseDto toListDto(Credito credito) {
        return CreditoResponseDto.builder()
                .id(credito.getId())
                .numeroCredito(credito.getNumeroCredito())
                .numeroNfse(credito.getNumeroNfse())
                .dataConstituicao(credito.getDataConstituicao())
                .valorIssqn(credito.getValorIssqn())
                .simplesNacional(credito.isSimplesNacional() ? "Sim" : "Não")
                .valorFaturado(credito.getValorFaturado())
                .status(credito.getStatus())
                .dataSolicitacao(credito.getDataSolicitacao())
                .dataAnalise(credito.getDataAnalise())
                .build();
    }

    private CreditoAdminResponseDto toAdminDto(Credito credito){
        return CreditoAdminResponseDto.builder()
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
                .dataSolicitacao(credito.getDataSolicitacao())
                .comentarioAnalise(credito.getComentarioAnalise())
                .dataAnalise(credito.getDataAnalise())
                .solicitadoPor(credito.getSolicitadoPor())
                .aprovadoPor(credito.getAprovadoPor())
                .build();
    }

}

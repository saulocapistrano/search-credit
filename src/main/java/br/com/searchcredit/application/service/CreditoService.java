package br.com.searchcredit.application.service;

import br.com.searchcredit.application.dto.credito.CreditoResponseDto;
import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.enums.StatusCredito;
import br.com.searchcredit.domain.repository.CreditoRepository;
import br.com.searchcredit.infrastructure.kafka.KafkaEventPublisher;
import br.com.searchcredit.infrastructure.kafka.event.ConsultaCreditoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CreditoService {

    private final CreditoRepository repository;
    private final KafkaEventPublisher kafkaEventPublisher;

    public CreditoService(CreditoRepository repository, KafkaEventPublisher kafkaEventPublisher) {
        this.repository = repository;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    public Optional<CreditoResponseDto> findByNumeroCredito(String numeroCredito){
        Optional<CreditoResponseDto> result = repository.findByNumeroCredito(numeroCredito)
                .map(this::toDto);
        try {
            kafkaEventPublisher.publishConsultaCredito(new ConsultaCreditoEvent("numeroCredito", numeroCredito));
        } catch (Exception e) {
            log.warn("Falha ao publicar evento Kafka para consulta por número de crédito: {}", numeroCredito, e);
        }
        return result;
    }

    public List<CreditoResponseDto> findAllByNumeroNfse(String numeroNfse){
        List<CreditoResponseDto> result = repository.findAllByNumeroNfse(numeroNfse)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        try {
            kafkaEventPublisher.publishConsultaCredito(new ConsultaCreditoEvent("numeroNfse", numeroNfse));
        } catch (Exception e) {
            log.warn("Falha ao publicar evento Kafka para consulta por número NFS-e: {}", numeroNfse, e);
        }
        return result;
    }

    public Page<CreditoResponseDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "dataConstituicao")
        );

        return repository.findAll(pageable)
                .map(this::toDto);
    }

    public Optional<CreditoResponseDto> findById(Long id) {
        return repository.findById(id)
                .map(this::toDto);
    }

    public Optional<CreditoResponseDto> updateStatus(Long id, StatusCredito novoStatus) {
        return repository.findById(id)
                .map(credito -> {
                    credito.setStatus(novoStatus);
                    Credito updated = repository.save(credito);
                    return toDto(updated);
                });
    }

    private CreditoResponseDto toDto(Credito credito){
        return CreditoResponseDto.builder()
                .id(credito.getId())
                .numeroCredito(credito.getNumeroCredito())
                .numeroNfse(credito.getNumeroNfse())
                .dataConstituicao(credito.getDataConstituicao())
                .valorIssqn(credito.getValorIssqn())
                .tipoCredito(credito.getTipoCredito())
                .simplesNacional(credito.isSimplesNacional())
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

}

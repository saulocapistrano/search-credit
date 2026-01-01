package br.com.searchcredit.application.service;

import br.com.searchcredit.application.dto.CreditoResponseDto;
import br.com.searchcredit.domain.entity.Credito;
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

    public List<CreditoResponseDto> findAllByNumeroFnse(String numeroFnse){
        List<CreditoResponseDto> result = repository.findAllByNumeroNfse(numeroFnse)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        try {
            kafkaEventPublisher.publishConsultaCredito(new ConsultaCreditoEvent("numeroNfse", numeroFnse));
        } catch (Exception e) {
            log.warn("Falha ao publicar evento Kafka para consulta por número NFS-e: {}", numeroFnse, e);
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

    private CreditoResponseDto toDto(Credito credito){
        return  CreditoResponseDto.builder()
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
                .build();
    }

}

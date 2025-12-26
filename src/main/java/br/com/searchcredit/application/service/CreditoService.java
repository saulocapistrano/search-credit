package br.com.searchcredit.application.service;

import br.com.searchcredit.application.dto.CreditoResponseDto;
import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.repository.CreditoRepository;
import br.com.searchcredit.infrastructure.kafka.KafkaEventPublisher;
import br.com.searchcredit.infrastructure.kafka.event.ConsultaCreditoEvent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        kafkaEventPublisher.publishConsultaCredito(new ConsultaCreditoEvent("numeroCredito", numeroCredito));
        return result;
    }

    public List<CreditoResponseDto> findAllByNumeroFnse(String numeroFnse){
        List<CreditoResponseDto> result = repository.findAllByNumeroNfse(numeroFnse)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        kafkaEventPublisher.publishConsultaCredito(new ConsultaCreditoEvent("numeroNfse", numeroFnse));
        return result;
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

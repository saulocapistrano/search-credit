package br.com.searchcredit.application.service;

import br.com.searchcredit.application.dto.CreditoResponseDto;
import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.infrastructure.repository.CreditoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CreditoService {

    private final CreditoRepository repository;

    public CreditoService(CreditoRepository repository) {
        this.repository = repository;
    }

    public Optional<CreditoResponseDto> findByNumeroCredito(String numeroCredito){
        return repository.findByNumeroCredito(numeroCredito)
                .map(this::toDto);
    }

    public List<CreditoResponseDto> findAllByNumeroFnse(String numeroFnse){
        return  repository.findAllByNumeroNfse(numeroFnse)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
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

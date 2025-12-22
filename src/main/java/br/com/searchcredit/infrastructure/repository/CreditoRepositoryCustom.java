package br.com.searchcredit.infrastructure.repository;

import br.com.searchcredit.domain.entity.Credito;

import java.util.List;
import java.util.Optional;

public interface CreditoRepositoryCustom {

    Optional<Credito> findByNumeroCredito(String numeroCredito);

    List<Credito> findAllByNumeroNfse(String numeroNfse);
}


package br.com.searchcredit.domain.repository;

import br.com.searchcredit.domain.entity.Credito;

import java.util.List;
import java.util.Optional;

public interface CreditoRepository {

    Optional<Credito> findByNumeroCredito(String numeroCredito);

    List<Credito> findAllByNumeroNfse(String numeroNfse);
}

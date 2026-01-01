package br.com.searchcredit.domain.repository;

import br.com.searchcredit.domain.entity.Credito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CreditoRepository {

    Optional<Credito> findByNumeroCredito(String numeroCredito);

    List<Credito> findAllByNumeroNfse(String numeroNfse);

    Page<Credito> findAll(Pageable pageable);
}

package br.com.searchcredit.domain.repository;

import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.enums.StatusCredito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CreditoRepository {

    Credito save(Credito credito);

    Optional<Credito> findByNumeroCredito(String numeroCredito);

    List<Credito> findAllByNumeroNfse(String numeroNfse);

    Page<Credito> findAll(Pageable pageable);

    Page<Credito> findByNomeSolicitante(String nomeSolicitante, Pageable pageable);

    List<Credito> findByStatus(StatusCredito status);

    Optional<Credito> findById(Long id);

    List<Credito> findAll();

    List<Credito> findByNumeroCreditoList(String numeroCredito);

    List<Credito> findByNumeroNfseList(String numeroNfse);

    List<Credito> findByNomeSolicitante(String nomeSolicitante);

    Page<Credito> findAll(int page, int size, String sortBy, String sortDir);

    List<Credito> findAllOrderByNumeroCreditoDesc();

    List<Credito> findAllOrderByNumeroNfseDesc();
}

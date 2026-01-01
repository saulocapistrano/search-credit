package br.com.searchcredit.infrastructure.repository.jpa;

import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.enums.StatusCredito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditoJpaRepository extends JpaRepository<Credito, Long> {

    Optional<Credito> findByNumeroCredito(String numeroCredito);
    List<Credito> findAllByNumeroNfse(String numeroNfse);

    Page<Credito> findByNomeSolicitante(String nomeSolicitante, Pageable pageable);

    List<Credito> findByStatus(StatusCredito status);

}

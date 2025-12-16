package br.com.searchcredit.infrastructure.repository.jpa;

import br.com.searchcredit.domain.entity.Credito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditoJpaRepository extends JpaRepository<Credito, Long> {

    Optional<Credito> findByNumeroCredito(String numeroCredito);
    List<Credito> findAllByNumeroNfse(String numeroNfse);

}

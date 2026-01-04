package br.com.searchcredit.infrastructure.repository.jpa;

import br.com.searchcredit.domain.entity.CreditoAnaliseAutomatica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditoAnaliseAutomaticaJpaRepository extends JpaRepository<CreditoAnaliseAutomatica, Long> {

    boolean existsByNumeroCredito(String numeroCredito);
}

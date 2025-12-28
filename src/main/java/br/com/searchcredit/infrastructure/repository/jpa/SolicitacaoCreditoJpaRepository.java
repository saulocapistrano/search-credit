package br.com.searchcredit.infrastructure.repository.jpa;

import br.com.searchcredit.domain.entity.SolicitacaoCredito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoCreditoJpaRepository extends JpaRepository<SolicitacaoCredito, Long> {

    List<SolicitacaoCredito> findByNomeSolicitante(String nomeSolicitante);

    List<SolicitacaoCredito> findByNumeroCredito(String numeroCredito);
}


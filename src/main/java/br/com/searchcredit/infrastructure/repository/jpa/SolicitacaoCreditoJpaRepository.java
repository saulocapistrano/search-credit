package br.com.searchcredit.infrastructure.repository.jpa;

import br.com.searchcredit.domain.entity.SolicitacaoCredito;
import br.com.searchcredit.domain.enums.StatusSolicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SolicitacaoCreditoJpaRepository extends JpaRepository<SolicitacaoCredito, Long> {

    List<SolicitacaoCredito> findByNomeSolicitante(String nomeSolicitante);

    Page<SolicitacaoCredito> findByNomeSolicitante(String nomeSolicitante, Pageable pageable);

    List<SolicitacaoCredito> findByNumeroCredito(String numeroCredito);

    List<SolicitacaoCredito> findByNumeroNfse(String numeroNfse);

    List<SolicitacaoCredito> findByStatusOrderByDataSolicitacaoAsc(StatusSolicitacao status);

    @Query("SELECT s FROM SolicitacaoCredito s WHERE s.numeroCredito LIKE 'CRED%' ORDER BY s.numeroCredito DESC")
    List<SolicitacaoCredito> findAllOrderByNumeroCreditoDesc();

    @Query("SELECT s FROM SolicitacaoCredito s WHERE s.numeroNfse LIKE 'NFSE%' ORDER BY s.numeroNfse DESC")
    List<SolicitacaoCredito> findAllOrderByNumeroNfseDesc();
}


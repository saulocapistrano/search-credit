package br.com.searchcredit.domain.repository;

import br.com.searchcredit.domain.entity.SolicitacaoCredito;
import br.com.searchcredit.domain.enums.StatusSolicitacao;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface SolicitacaoCreditoRepository {

    SolicitacaoCredito save(SolicitacaoCredito solicitacaoCredito);

    Optional<SolicitacaoCredito> findById(Long id);

    List<SolicitacaoCredito> findAll();

    List<SolicitacaoCredito> findByNomeSolicitante(String nomeSolicitante);

    List<SolicitacaoCredito> findByNumeroCredito(String numeroCredito);

    List<SolicitacaoCredito> findByNumeroNfse(String numeroNfse);

    List<SolicitacaoCredito> findByStatusOrderByDataSolicitacaoAsc(StatusSolicitacao status);

    Page<SolicitacaoCredito> findByNomeSolicitante(String nomeSolicitante, int page, int size);

    Page<SolicitacaoCredito> findAll(int page, int size, String sortBy, String sortDir);

    List<SolicitacaoCredito> findAllOrderByNumeroCreditoDesc();

    List<SolicitacaoCredito> findAllOrderByNumeroNfseDesc();
}


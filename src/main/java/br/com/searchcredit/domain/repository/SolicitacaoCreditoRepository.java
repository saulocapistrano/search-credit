package br.com.searchcredit.domain.repository;

import br.com.searchcredit.domain.entity.SolicitacaoCredito;

import java.util.List;
import java.util.Optional;

public interface SolicitacaoCreditoRepository {

    SolicitacaoCredito save(SolicitacaoCredito solicitacaoCredito);

    Optional<SolicitacaoCredito> findById(Long id);

    List<SolicitacaoCredito> findAll();

    List<SolicitacaoCredito> findByNomeSolicitante(String nomeSolicitante);

    List<SolicitacaoCredito> findByNumeroCredito(String numeroCredito);
}


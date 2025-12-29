package br.com.searchcredit.infrastructure.repository.impl;

import br.com.searchcredit.domain.entity.SolicitacaoCredito;
import br.com.searchcredit.domain.enums.StatusSolicitacao;
import br.com.searchcredit.domain.repository.SolicitacaoCreditoRepository;
import br.com.searchcredit.infrastructure.repository.jpa.SolicitacaoCreditoJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SolicitacaoCreditoRepositoryImpl implements SolicitacaoCreditoRepository {

    private final SolicitacaoCreditoJpaRepository jpaRepository;

    public SolicitacaoCreditoRepositoryImpl(SolicitacaoCreditoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SolicitacaoCredito save(SolicitacaoCredito solicitacaoCredito) {
        return jpaRepository.save(solicitacaoCredito);
    }

    @Override
    public Optional<SolicitacaoCredito> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<SolicitacaoCredito> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<SolicitacaoCredito> findByNomeSolicitante(String nomeSolicitante) {
        return jpaRepository.findByNomeSolicitante(nomeSolicitante);
    }

    @Override
    public List<SolicitacaoCredito> findByNumeroCredito(String numeroCredito) {
        return jpaRepository.findByNumeroCredito(numeroCredito);
    }

    @Override
    public List<SolicitacaoCredito> findByNumeroNfse(String numeroNfse) {
        return jpaRepository.findByNumeroNfse(numeroNfse);
    }

    @Override
    public List<SolicitacaoCredito> findByStatusOrderByDataSolicitacaoAsc(StatusSolicitacao status) {
        return jpaRepository.findByStatusOrderByDataSolicitacaoAsc(status);
    }
}


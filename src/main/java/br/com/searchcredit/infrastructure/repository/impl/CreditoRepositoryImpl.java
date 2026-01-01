package br.com.searchcredit.infrastructure.repository.impl;

import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.enums.StatusCredito;
import br.com.searchcredit.domain.repository.CreditoRepository;
import br.com.searchcredit.infrastructure.repository.jpa.CreditoJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CreditoRepositoryImpl implements CreditoRepository {

    private final CreditoJpaRepository jpaRepository;

    public CreditoRepositoryImpl(CreditoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Credito save(Credito credito) {
        return jpaRepository.save(credito);
    }

    @Override
    public Optional<Credito> findByNumeroCredito(String numeroCredito) {
        return jpaRepository.findByNumeroCredito(numeroCredito);
    }

    @Override
    public List<Credito> findAllByNumeroNfse(String numeroNfse) {
        return jpaRepository.findAllByNumeroNfse(numeroNfse);
    }

    @Override
    public Page<Credito> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public Page<Credito> findByNomeSolicitante(String nomeSolicitante, Pageable pageable) {
        return jpaRepository.findByNomeSolicitante(nomeSolicitante, pageable);
    }

    @Override
    public List<Credito> findByStatus(StatusCredito status) {
        return jpaRepository.findByStatus(status);
    }

    @Override
    public Optional<Credito> findById(Long id) {
        return jpaRepository.findById(id);
    }

}

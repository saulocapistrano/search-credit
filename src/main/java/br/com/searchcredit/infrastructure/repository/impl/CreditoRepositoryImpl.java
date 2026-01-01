package br.com.searchcredit.infrastructure.repository.impl;

import br.com.searchcredit.domain.entity.Credito;
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

}

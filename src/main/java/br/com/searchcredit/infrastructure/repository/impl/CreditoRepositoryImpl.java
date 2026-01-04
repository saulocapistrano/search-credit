package br.com.searchcredit.infrastructure.repository.impl;

import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.enums.StatusCredito;
import br.com.searchcredit.domain.repository.CreditoRepository;
import br.com.searchcredit.infrastructure.repository.jpa.CreditoJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public List<Credito> findByStatus(StatusCredito status) {
        return jpaRepository.findByStatus(status);
    }

    @Override
    public Optional<Credito> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Credito> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Credito> findByNumeroCreditoList(String numeroCredito) {
        return jpaRepository.findByNumeroCreditoList(numeroCredito);
    }

    @Override
    public List<Credito> findByNumeroNfseList(String numeroNfse) {
        return jpaRepository.findByNumeroNfseList(numeroNfse);
    }

    @Override
    public Page<Credito> findAll(int page, int size, String sortBy, String sortDir) {
        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase("desc")
                ? org.springframework.data.domain.Sort.by(sortBy).descending()
                : org.springframework.data.domain.Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return jpaRepository.findAll(pageable);
    }

    @Override
    public List<Credito> findAllOrderByNumeroCreditoDesc() {
        return jpaRepository.findAllOrderByNumeroCreditoDesc();
    }

    @Override
    public List<Credito> findAllOrderByNumeroNfseDesc() {
        return jpaRepository.findAllOrderByNumeroNfseDesc();
    }

}

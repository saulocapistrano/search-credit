package br.com.searchcredit.infrastructure.repository.jpa;

import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.enums.StatusCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CreditoJpaRepository extends JpaRepository<Credito, Long> {

    Optional<Credito> findByNumeroCredito(String numeroCredito);
    
    List<Credito> findAllByNumeroNfse(String numeroNfse);

    List<Credito> findByStatus(StatusCredito status);

    @Query("SELECT c FROM Credito c WHERE c.numeroCredito = :numeroCredito")
    List<Credito> findByNumeroCreditoList(String numeroCredito);

    @Query("SELECT c FROM Credito c WHERE c.numeroNfse = :numeroNfse")
    List<Credito> findByNumeroNfseList(String numeroNfse);

    @Query("SELECT c FROM Credito c WHERE c.numeroCredito LIKE 'CRED%' ORDER BY c.numeroCredito DESC")
    List<Credito> findAllOrderByNumeroCreditoDesc();

    @Query("SELECT c FROM Credito c WHERE c.numeroNfse LIKE 'NFSE%' ORDER BY c.numeroNfse DESC")
    List<Credito> findAllOrderByNumeroNfseDesc();

}

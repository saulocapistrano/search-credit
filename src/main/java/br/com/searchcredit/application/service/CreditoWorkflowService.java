package br.com.searchcredit.application.service;

import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.enums.StatusCredito;
import br.com.searchcredit.domain.repository.CreditoRepository;
import br.com.searchcredit.infrastructure.storage.MinioStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Service responsável por criar créditos com workflow (status EM_ANALISE).
 * 
 * Este service substitui a criação de SolicitacaoCredito, criando diretamente
 * registros na tabela Credito com os campos de workflow preenchidos.
 */
@Service
@RequiredArgsConstructor
public class CreditoWorkflowService {

    private final CreditoRepository creditoRepository;
    private final MinioStorageService minioStorageService;

    /**
     * Cria um novo crédito com status inicial EM_ANALISE.
     * 
     * @param numeroCredito Número do crédito
     * @param numeroNfse Número da NFS-e
     * @param dataConstituicao Data de constituição
     * @param valorIssqn Valor do ISSQN
     * @param tipoCredito Tipo do crédito
     * @param simplesNacional Indica se é Simples Nacional
     * @param aliquota Alíquota aplicada
     * @param valorFaturado Valor faturado
     * @param valorDeducao Valor de dedução
     * @param baseCalculo Base de cálculo
     * @param nomeSolicitante Nome do solicitante
     * @param comprovante Arquivo de comprovante (opcional)
     * @return Credito criado e persistido
     */
    @Transactional
    public Credito criarCreditoComWorkflow(
            String numeroCredito,
            String numeroNfse,
            java.time.LocalDate dataConstituicao,
            java.math.BigDecimal valorIssqn,
            String tipoCredito,
            Boolean simplesNacional,
            java.math.BigDecimal aliquota,
            java.math.BigDecimal valorFaturado,
            java.math.BigDecimal valorDeducao,
            java.math.BigDecimal baseCalculo,
            String nomeSolicitante,
            MultipartFile comprovante) {

        String comprovanteUrl = null;
        if (comprovante != null && !comprovante.isEmpty()) {
            comprovanteUrl = minioStorageService.uploadComprovante(comprovante);
        }

        Credito credito = Credito.builder()
                .numeroCredito(numeroCredito)
                .numeroNfse(numeroNfse)
                .dataConstituicao(dataConstituicao)
                .valorIssqn(valorIssqn)
                .tipoCredito(tipoCredito)
                .simplesNacional(simplesNacional != null ? simplesNacional : false)
                .aliquota(aliquota)
                .valorFaturado(valorFaturado)
                .valorDeducao(valorDeducao)
                .baseCalculo(baseCalculo)
                .status(StatusCredito.EM_ANALISE)
                .nomeSolicitante(nomeSolicitante)
                .comprovanteUrl(comprovanteUrl)
                .dataSolicitacao(LocalDateTime.now())
                .build();

        return creditoRepository.save(credito);
    }
}


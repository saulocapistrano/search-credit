package br.com.searchcredit.domain.enums;

/**
 * Enum que representa os estados válidos de um crédito.
 * 
 * Estados:
 * - CONSTITUIDO: Crédito já constituído/aprovado
 * - EM_ANALISE: Estado inicial da solicitação (em análise)
 * - APROVADO: Solicitação aprovada pelo Admin Full
 * - REPROVADO: Solicitação reprovada pelo Admin Full
 */
public enum StatusCredito {
    CONSTITUIDO,
    EM_ANALISE,
    APROVADO,
    REPROVADO
}


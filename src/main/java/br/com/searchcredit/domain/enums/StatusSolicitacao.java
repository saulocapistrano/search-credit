package br.com.searchcredit.domain.enums;

/**
 * Enum que representa os estados válidos de uma solicitação de crédito.
 * 
 * Estados:
 * - EM_ANALISE: Estado inicial da solicitação
 * - APROVADO: Solicitação aprovada pelo Admin Full
 * - REPROVADO: Solicitação reprovada pelo Admin Full
 */
public enum StatusSolicitacao {
    EM_ANALISE,
    APROVADO,
    REPROVADO
}


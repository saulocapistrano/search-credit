package br.com.searchcredit.application.dto.credito;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de requisição para criação de um novo crédito.
 * 
 * Representa os dados necessários para criar um crédito com workflow inicial.
 * O status será automaticamente definido como EM_ANALISE.
 */
@Data
public class CreditoCreateRequestDto {

    @NotBlank(message = "Número da NFS-e é obrigatório")
    private String numeroNfse;

    @NotNull(message = "Data de constituição é obrigatória")
    private LocalDate dataConstituicao;

    @NotNull(message = "Valor do ISSQN é obrigatório")
    private BigDecimal valorIssqn;

    @NotBlank(message = "Tipo do crédito é obrigatório")
    private String tipoCredito;

    private Boolean simplesNacional;

    @NotNull(message = "Alíquota é obrigatória")
    private BigDecimal aliquota;

    @NotNull(message = "Valor faturado é obrigatório")
    private BigDecimal valorFaturado;

    @NotNull(message = "Valor de dedução é obrigatório")
    private BigDecimal valorDeducao;

    @NotNull(message = "Base de cálculo é obrigatória")
    private BigDecimal baseCalculo;

    @NotBlank(message = "Nome do solicitante é obrigatório")
    private String nomeSolicitante;

    /**
     * Arquivo de comprovante (opcional).
     * Será enviado como MultipartFile no form-data.
     */
    private MultipartFile comprovante;
}


package br.com.searchcredit.interfaces.controller;

import br.com.searchcredit.application.dto.credito.CreditoAnaliseRequestDto;
import br.com.searchcredit.application.dto.credito.CreditoCreateRequestDto;
import br.com.searchcredit.application.dto.credito.CreditoResponseDto;
import br.com.searchcredit.application.dto.credito.CreditoStatusUpdateRequestDto;
import br.com.searchcredit.application.service.CreditoService;
import br.com.searchcredit.application.service.CreditoWorkflowService;
import br.com.searchcredit.application.service.SolicitacaoCreditoApplicationService;
import br.com.searchcredit.application.service.SolicitacaoCreditoService;
import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.enums.StatusCredito;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller REST definitivo para operações com Crédito.
 * 
 * Este é o endpoint oficial da API. Todos os recursos relacionados a créditos
 * devem ser acessados através de /api/creditos.
 * 
 * @see CreditoSolicitacaoController (legado - deprecated)
 */
@RestController
@RequestMapping("/api/creditos")
@RequiredArgsConstructor
public class CreditoController {

    private final CreditoService creditoService;
    private final CreditoWorkflowService creditoWorkflowService;
    private final SolicitacaoCreditoApplicationService analiseService;
    private final SolicitacaoCreditoService solicitacaoCreditoService; // Para gerar números

    /**
     * Lista todos os créditos com paginação.
     * 
     * @param page Número da página (default: 0)
     * @param size Tamanho da página (default: 10)
     * @return Página de créditos
     */
    @GetMapping
    public ResponseEntity<Page<CreditoResponseDto>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CreditoResponseDto> creditos = creditoService.findAll(page, size);
        return ResponseEntity.ok().body(creditos);
    }

    /**
     * Busca um crédito por ID.
     * 
     * @param id ID do crédito
     * @return Crédito encontrado ou 404 se não encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<CreditoResponseDto> findById(@PathVariable Long id) {
        return creditoService.findById(id)
                .map(dto -> ResponseEntity.ok().body(dto))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca créditos por número de NFS-e.
     * 
     * @param numeroNfse Número da NFS-e
     * @return Lista de créditos encontrados
     */
    @GetMapping("/nfse/{numeroNfse}")
    public ResponseEntity<List<CreditoResponseDto>> findByNumeroNfse(@PathVariable String numeroNfse) {
        List<CreditoResponseDto> creditos = creditoService.findAllByNumeroNfse(numeroNfse);
        if (creditos == null || creditos.isEmpty()) {
            return ResponseEntity.ok().body(List.of());
        }
        return ResponseEntity.ok().body(creditos);
    }

    /**
     * Busca um crédito por número de crédito.
     * 
     * @param numeroCredito Número do crédito
     * @return Crédito encontrado ou 404 se não encontrado
     */
    @GetMapping("/numero/{numeroCredito}")
    public ResponseEntity<CreditoResponseDto> findByNumeroCredito(@PathVariable String numeroCredito) {
        return creditoService.findByNumeroCredito(numeroCredito)
                .map(dto -> ResponseEntity.ok().body(dto))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cria um novo crédito com workflow inicial (status EM_ANALISE).
     * 
     * @param requestDto Dados do crédito a ser criado
     * @param comprovante Arquivo de comprovante (opcional)
     * @return Crédito criado
     */
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<CreditoResponseDto> create(
            @ModelAttribute @Valid CreditoCreateRequestDto requestDto,
            @RequestPart(value = "comprovante", required = false) MultipartFile comprovante) {
        
        // Gerar número de crédito
        String numeroCredito = solicitacaoCreditoService.gerarProximoNumeroCredito();

        Credito creditoCriado = creditoWorkflowService.criarCreditoComWorkflow(
                numeroCredito,
                requestDto.getNumeroNfse(),
                requestDto.getDataConstituicao(),
                requestDto.getValorIssqn(),
                requestDto.getTipoCredito(),
                requestDto.getSimplesNacional(),
                requestDto.getAliquota(),
                requestDto.getValorFaturado(),
                requestDto.getValorDeducao(),
                requestDto.getBaseCalculo(),
                requestDto.getNomeSolicitante(),
                comprovante != null ? comprovante : requestDto.getComprovante()
        );

        CreditoResponseDto responseDto = toResponseDto(creditoCriado);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * Atualiza o status de um crédito.
     * 
     * Não permite alterar diretamente para APROVADO ou REPROVADO.
     * Use o endpoint /analise para esses casos.
     * 
     * @param id ID do crédito
     * @param requestDto Novo status
     * @return Crédito atualizado ou 404 se não encontrado
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<CreditoResponseDto> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid CreditoStatusUpdateRequestDto requestDto) {
        
        // Validar que não está tentando aprovar/reprovar diretamente
        if (requestDto.getStatus() == StatusCredito.APROVADO || 
            requestDto.getStatus() == StatusCredito.REPROVADO) {
            return ResponseEntity.badRequest().build();
        }

        return creditoService.updateStatus(id, requestDto.getStatus())
                .map(dto -> ResponseEntity.ok().body(dto))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Analisa um crédito (aprova ou reprova).
     * 
     * O crédito deve estar com status EM_ANALISE.
     * 
     * @param id ID do crédito
     * @param requestDto Dados da análise (status e comentário)
     * @return 204 No Content se sucesso, 404 se não encontrado, 400 se inválido
     */
    @PutMapping("/{id}/analise")
    public ResponseEntity<Void> analisar(
            @PathVariable Long id,
            @RequestBody @Valid CreditoAnaliseRequestDto requestDto) {
        
        // Validar que o status é APROVADO ou REPROVADO
        if (requestDto.getStatus() != StatusCredito.APROVADO && 
            requestDto.getStatus() != StatusCredito.REPROVADO) {
            return ResponseEntity.badRequest().build();
        }

        try {
            analiseService.responderAnalise(
                    id,
                    requestDto.getStatus(),
                    requestDto.getComentarioAnalise()
            );
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Converte Credito para CreditoResponseDto.
     */
    private CreditoResponseDto toResponseDto(Credito credito) {
        return CreditoResponseDto.builder()
                .id(credito.getId())
                .numeroCredito(credito.getNumeroCredito())
                .numeroNfse(credito.getNumeroNfse())
                .dataConstituicao(credito.getDataConstituicao())
                .valorIssqn(credito.getValorIssqn())
                .tipoCredito(credito.getTipoCredito())
                .simplesNacional(credito.isSimplesNacional())
                .aliquota(credito.getAliquota())
                .valorFaturado(credito.getValorFaturado())
                .valorDeducao(credito.getValorDeducao())
                .baseCalculo(credito.getBaseCalculo())
                .status(credito.getStatus())
                .nomeSolicitante(credito.getNomeSolicitante())
                .comprovanteUrl(credito.getComprovanteUrl())
                .dataSolicitacao(credito.getDataSolicitacao())
                .comentarioAnalise(credito.getComentarioAnalise())
                .dataAnalise(credito.getDataAnalise())
                .build();
    }

}

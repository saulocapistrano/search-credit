package br.com.searchcredit.interfaces.controller;

import br.com.searchcredit.application.dto.credito.CreditoAnaliseRequestDto;
import br.com.searchcredit.application.dto.credito.CreditoCreateRequestDto;
import br.com.searchcredit.application.dto.credito.CreditoAdminResponseDto;
import br.com.searchcredit.application.dto.credito.CreditoQueryResponseDto;
import br.com.searchcredit.application.dto.credito.CreditoResponseDto;
import br.com.searchcredit.application.dto.common.NextValueResponseDto;
import br.com.searchcredit.application.service.CreditoService;
import br.com.searchcredit.application.service.CreditoNumeroGeneratorService;
import br.com.searchcredit.domain.enums.StatusCredito;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
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
    private final CreditoNumeroGeneratorService creditoNumeroGeneratorService;

    @GetMapping
    public ResponseEntity<Page<CreditoResponseDto>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dataConstituicao") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        return ResponseEntity.ok(creditoService.listAll(page, size, sortBy, sortDir));
    }

    @GetMapping("/next-numero-credito")
    public ResponseEntity<NextValueResponseDto> nextNumeroCredito() {
        return ResponseEntity.ok(new NextValueResponseDto(creditoNumeroGeneratorService.nextNumeroCredito()));
    }

    @GetMapping("/next-numero-nfse")
    public ResponseEntity<NextValueResponseDto> nextNumeroNfse() {
        return ResponseEntity.ok(new NextValueResponseDto(creditoNumeroGeneratorService.nextNumeroNfse()));
    }

    /**
     * Busca créditos por número de NFS-e.
     * 
     * @param numeroNfse Número da NFS-e
     * @return Lista de créditos encontrados
     */
    @GetMapping("/{numeroNfse}")
    public ResponseEntity<List<CreditoQueryResponseDto>> findByNumeroNfse(@PathVariable String numeroNfse) {
        List<CreditoQueryResponseDto> creditos = creditoService.findAllByNumeroNfse(numeroNfse);
        return ResponseEntity.ok().body(creditos);
    }

    /**
     * Busca um crédito por número de crédito.
     * 
     * @param numeroCredito Número do crédito
     * @return Crédito encontrado ou 404 se não encontrado
     */
    @GetMapping("/credito/{numeroCredito}")
    public ResponseEntity<CreditoQueryResponseDto> findByNumeroCredito(@PathVariable String numeroCredito) {
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreditoAdminResponseDto> create(
            @RequestPart("credito") @Valid CreditoCreateRequestDto requestDto,
            @RequestPart(value = "comprovante", required = false) MultipartFile comprovante) {
        CreditoAdminResponseDto created = creditoService.create(requestDto, comprovante);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
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
            creditoService.analisar(id, requestDto);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

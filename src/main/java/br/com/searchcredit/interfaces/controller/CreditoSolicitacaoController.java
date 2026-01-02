package br.com.searchcredit.interfaces.controller;

import br.com.searchcredit.application.dto.solicitacao.AnaliseSolicitacaoRequestDto;
import br.com.searchcredit.application.dto.solicitacao.AtualizarStatusRequestDto;
import br.com.searchcredit.application.dto.solicitacao.SolicitacaoCreditoRequestDto;
import br.com.searchcredit.application.dto.solicitacao.SolicitacaoCreditoResponseDto;
import br.com.searchcredit.application.service.SolicitacaoCreditoApplicationService;
import br.com.searchcredit.application.service.SolicitacaoCreditoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller legado para operações com solicitações de crédito.
 * 
 * @deprecated Este controller é legado e será removido em versão futura.
 * Use {@link CreditoController} com o endpoint /api/creditos.
 * 
 * Este controller mantém compatibilidade com clientes existentes,
 * mas novos desenvolvimentos devem usar /api/creditos.
 */
@Deprecated
@RestController
@RequestMapping("/api/solicitacoes")
@RequiredArgsConstructor
public class CreditoSolicitacaoController {

    private final SolicitacaoCreditoService solicitacaoCreditoService;
    private final SolicitacaoCreditoApplicationService solicitacaoCreditoApplicationService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<SolicitacaoCreditoResponseDto> criarSolicitacao(
            @ModelAttribute @Valid SolicitacaoCreditoRequestDto requestDto,
            @RequestPart(value = "comprovante", required = false) MultipartFile comprovante) {
        SolicitacaoCreditoResponseDto response = solicitacaoCreditoService.criarSolicitacao(requestDto, comprovante);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/next-numero-credito")
    public ResponseEntity<String> gerarNumeroCredito() {
        String numero = solicitacaoCreditoService.gerarProximoNumeroCredito();
        return ResponseEntity.ok().body(numero);
    }

    @GetMapping("/next-numero-nfse")
    public ResponseEntity<String> gerarNumeroNfse() {
        String numero = solicitacaoCreditoService.gerarProximoNumeroNfse();
        return ResponseEntity.ok().body(numero);
    }

    @GetMapping
    public ResponseEntity<Page<SolicitacaoCreditoResponseDto>> listarPorSolicitante(
            @RequestParam String nomeSolicitante,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SolicitacaoCreditoResponseDto> pagina = solicitacaoCreditoService
                .listarPorSolicitante(nomeSolicitante, page, size);
        return ResponseEntity.ok().body(pagina);
    }

    @GetMapping("/todas")
    public ResponseEntity<Page<SolicitacaoCreditoResponseDto>> listarTodas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dataSolicitacao") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Page<SolicitacaoCreditoResponseDto> pagina = solicitacaoCreditoService
                .listarTodas(page, size, sortBy, sortDir);
        return ResponseEntity.ok().body(pagina);
    }

    @GetMapping("/nfse/{numeroNfse}")
    public ResponseEntity<?> buscarPorNumeroNfse(
            @PathVariable String numeroNfse) {
        List<SolicitacaoCreditoResponseDto> solicitacoes = 
                solicitacaoCreditoService.listarPorNumeroNfse(numeroNfse);
        
        if (solicitacoes == null || solicitacoes.isEmpty()) {
            Map<String, String> erro = new HashMap<>();
            erro.put("mensagem", "Nenhum crédito encontrado para o número de NFSe informado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
        }
        
        return ResponseEntity.ok().body(solicitacoes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoCreditoResponseDto> buscarPorId(@PathVariable Long id) {
        return solicitacaoCreditoService.buscarPorId(id)
                .map(dto -> ResponseEntity.ok().body(dto))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<SolicitacaoCreditoResponseDto> atualizarStatus(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarStatusRequestDto requestDto) {
        return solicitacaoCreditoService.atualizarStatus(id, requestDto)
                .map(dto -> ResponseEntity.ok().body(dto))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/analise")
    public ResponseEntity<Void> analisarSolicitacao(
            @PathVariable Long id,
            @RequestBody @Valid AnaliseSolicitacaoRequestDto requestDto) {
        try {
            solicitacaoCreditoApplicationService.responderAnalise(
                    id,
                    requestDto.getStatus(),
                    requestDto.getComentario()
            );
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}


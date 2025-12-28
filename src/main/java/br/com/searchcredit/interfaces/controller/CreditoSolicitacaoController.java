package br.com.searchcredit.interfaces.controller;

import br.com.searchcredit.application.dto.solicitacao.AtualizarStatusRequestDto;
import br.com.searchcredit.application.dto.solicitacao.SolicitacaoCreditoRequestDto;
import br.com.searchcredit.application.dto.solicitacao.SolicitacaoCreditoResponseDto;
import br.com.searchcredit.application.service.SolicitacaoCreditoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/solicitacoes")
@RequiredArgsConstructor
public class CreditoSolicitacaoController {

    private final SolicitacaoCreditoService solicitacaoCreditoService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<SolicitacaoCreditoResponseDto> criarSolicitacao(
            @ModelAttribute @Valid SolicitacaoCreditoRequestDto requestDto,
            @RequestPart(value = "comprovante", required = false) MultipartFile comprovante) {
        SolicitacaoCreditoResponseDto response = solicitacaoCreditoService.criarSolicitacao(requestDto, comprovante);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SolicitacaoCreditoResponseDto>> listarSolicitacoes(
            @RequestParam(required = false) String nomeSolicitante) {
        
        List<SolicitacaoCreditoResponseDto> solicitacoes;
        
        if (nomeSolicitante != null && !nomeSolicitante.isBlank()) {
            solicitacoes = solicitacaoCreditoService.listarPorSolicitante(nomeSolicitante);
        } else {
            solicitacoes = solicitacaoCreditoService.listarTodas();
        }
        
        return ResponseEntity.ok(solicitacoes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoCreditoResponseDto> buscarPorId(@PathVariable Long id) {
        return solicitacaoCreditoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<SolicitacaoCreditoResponseDto> atualizarStatus(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarStatusRequestDto requestDto) {
        return solicitacaoCreditoService.atualizarStatus(id, requestDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}


package br.com.searchcredit.interfaces.controller;

import br.com.searchcredit.application.dto.CreditoResponseDto;
import br.com.searchcredit.application.service.CreditoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/creditos")
@RequiredArgsConstructor
public class CreditoController {

    private final CreditoService creditoService;

    @GetMapping("/{numeroNfse}")
    public ResponseEntity<List<CreditoResponseDto>> findByNumeroNfse(@PathVariable String numeroNfse) {
        List<CreditoResponseDto> creditos = creditoService.findAllByNumeroFnse(numeroNfse);
        return ResponseEntity.ok(creditos);
    }

    @GetMapping("/credito/{numeroCredito}")
    public ResponseEntity<CreditoResponseDto> findByNumeroCredito(@PathVariable String numeroCredito) {
        return creditoService.findByNumeroCredito(numeroCredito)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

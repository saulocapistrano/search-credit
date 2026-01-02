package br.com.searchcredit.interfaces.controller;

import br.com.searchcredit.application.dto.credito.CreditoResponseDto;
import br.com.searchcredit.application.service.CreditoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/creditos")
@RequiredArgsConstructor
public class AdminCreditoController {

    private final CreditoService creditoService;

    @GetMapping
    public ResponseEntity<Page<CreditoResponseDto>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(creditoService.findAll(page, size));
    }
}


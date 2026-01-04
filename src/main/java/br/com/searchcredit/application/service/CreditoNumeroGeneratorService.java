package br.com.searchcredit.application.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CreditoNumeroGeneratorService {

    private final JdbcTemplate jdbcTemplate;

    public CreditoNumeroGeneratorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String nextNumeroCredito() {
        Long value = jdbcTemplate.queryForObject("select nextval('credito_numero_credito_seq')", Long.class);
        return value != null ? value.toString() : null;
    }

    public String nextNumeroNfse() {
        Long value = jdbcTemplate.queryForObject("select nextval('credito_numero_nfse_seq')", Long.class);
        return value != null ? value.toString() : null;
    }
}

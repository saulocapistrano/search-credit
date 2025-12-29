package br.com.searchcredit.infrastructure.config;

import br.com.searchcredit.domain.entity.SolicitacaoCredito;
import br.com.searchcredit.domain.enums.StatusSolicitacao;
import br.com.searchcredit.domain.repository.SolicitacaoCreditoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeederConfig implements CommandLineRunner {

    private final SolicitacaoCreditoRepository repository;
    private final Random random = new Random();

    @Override
    public void run(String... args) {
        if (repository.findAll().isEmpty()) {
            log.info("Banco de dados vazio. Iniciando seed de dados de teste...");
            List<SolicitacaoCredito> solicitacoes = criarSolicitacoes();
            for (SolicitacaoCredito solicitacao : solicitacoes) {
                repository.save(solicitacao);
            }
            log.info("Seed concluído: {} solicitações de crédito inseridas", solicitacoes.size());
        } else {
            log.info("Banco de dados já contém dados. Seed não será executado.");
        }
    }

    private List<SolicitacaoCredito> criarSolicitacoes() {
        List<SolicitacaoCredito> solicitacoes = new ArrayList<>();
        
        for (int i = 1; i <= 15; i++) {
            SolicitacaoCredito solicitacao = SolicitacaoCredito.builder()
                    .numeroCredito("CRED" + String.format("%06d", i))
                    .numeroNfse("NFSE" + String.format("%07d", 1000000 + i))
                    .dataConstituicao(LocalDate.now().minusDays(random.nextInt(365) + 1))
                    .valorIssqn(gerarValorAleatorio(500.00, 5000.00))
                    .tipoCredito(gerarTipoCreditoAleatorio())
                    .simplesNacional(random.nextBoolean())
                    .aliquota(gerarValorAleatorio(3.0, 6.0))
                    .valorFaturado(gerarValorAleatorio(10000.00, 100000.00))
                    .valorDeducao(gerarValorAleatorio(1000.00, 15000.00))
                    .baseCalculo(gerarValorAleatorio(9000.00, 85000.00))
                    .status(StatusSolicitacao.EM_ANALISE)
                    .nomeSolicitante("Usuário Teste")
                    .comprovanteUrl(null)
                    .dataSolicitacao(LocalDateTime.now().minusDays(random.nextInt(15) + 1))
                    .comentarioAnalise(null)
                    .dataAnalise(null)
                    .build();
            
            solicitacoes.add(solicitacao);
        }
        
        return solicitacoes;
    }

    private BigDecimal gerarValorAleatorio(double min, double max) {
        double valor = min + (max - min) * random.nextDouble();
        return BigDecimal.valueOf(Math.round(valor * 100.0) / 100.0);
    }

    private String gerarTipoCreditoAleatorio() {
        String[] tipos = {"ISSQN", "ICMS", "IPI", "IRRF", "PIS", "COFINS"};
        return tipos[random.nextInt(tipos.length)];
    }
}


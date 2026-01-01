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
        
        // Dados baseados no script SQL fornecido - apenas números
        Object[][] dados = {
            // numeroCredito, numeroNfse, dataConstituicao (dias atrás), valorIssqn, tipoCredito, simplesNacional, aliquota, valorFaturado, valorDeducao, baseCalculo
            {"123456", "7891011", 30, 1500.75, "ISSQN", true, 5.0, 30000.00, 5000.00, 25000.00},
            {"789012", "7891011", 29, 1200.50, "ISSQN", false, 4.5, 25000.00, 4000.00, 21000.00},
            {"654321", "1122334", 75, 800.50, "Outros", true, 3.5, 20000.00, 3000.00, 17000.00},
            {"654322", "1122335", 14, 2200.00, "ISSQN", true, 5.5, 40000.00, 6000.00, 34000.00},
            {"654323", "1122336", 13, 950.25, "Outros", false, 3.0, 19000.00, 2500.00, 16500.00},
            {"654324", "1122337", 12, 1800.00, "ISSQN", true, 4.0, 36000.00, 8000.00, 28000.00},
            {"654325", "1122338", 11, 750.80, "Outros", false, 2.5, 15000.00, 2000.00, 13000.00},
            {"654326", "1122339", 10, 3200.00, "ISSQN", true, 6.0, 50000.00, 10000.00, 40000.00},
            {"654327", "1122340", 9, 1200.00, "Outros", true, 4.0, 24000.00, 4000.00, 20000.00},
            {"654328", "1122341", 8, 875.50, "ISSQN", false, 3.5, 17500.00, 3000.00, 14500.00},
            {"654329", "1122342", 7, 1450.00, "Outros", true, 5.0, 29000.00, 5000.00, 24000.00},
            {"654330", "1122343", 6, 1900.25, "ISSQN", false, 4.5, 38000.00, 7000.00, 31000.00},
            {"654331", "1122344", 5, 1100.00, "Outros", true, 3.0, 22000.00, 3000.00, 19000.00},
            {"654332", "1122345", 4, 2600.75, "ISSQN", true, 5.5, 45000.00, 9000.00, 36000.00},
            {"654333", "1122346", 3, 680.90, "Outros", false, 2.0, 13600.00, 1800.00, 11800.00},
            {"654334", "1122347", 2, 1550.00, "ISSQN", true, 4.0, 31000.00, 5000.00, 26000.00}
        };
        
        for (Object[] dadosItem : dados) {
            SolicitacaoCredito solicitacao = SolicitacaoCredito.builder()
                    .numeroCredito((String) dadosItem[0])
                    .numeroNfse((String) dadosItem[1])
                    .dataConstituicao(LocalDate.now().minusDays((Integer) dadosItem[2]))
                    .valorIssqn(BigDecimal.valueOf((Double) dadosItem[3]))
                    .tipoCredito((String) dadosItem[4])
                    .simplesNacional((Boolean) dadosItem[5])
                    .aliquota(BigDecimal.valueOf((Double) dadosItem[6]))
                    .valorFaturado(BigDecimal.valueOf((Double) dadosItem[7]))
                    .valorDeducao(BigDecimal.valueOf((Double) dadosItem[8]))
                    .baseCalculo(BigDecimal.valueOf((Double) dadosItem[9]))
                    .status(StatusSolicitacao.EM_ANALISE)
                    .nomeSolicitante("Usuário Teste")
                    .comprovanteUrl(null)
                    .dataSolicitacao(LocalDateTime.now().minusDays((Integer) dadosItem[2]))
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


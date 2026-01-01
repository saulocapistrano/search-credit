package br.com.searchcredit.application.mapper;

import br.com.searchcredit.application.dto.solicitacao.SolicitacaoCreditoResponseDto;
import br.com.searchcredit.domain.entity.SolicitacaoCredito;
import org.springframework.stereotype.Component;

@Component
public class SolicitacaoCreditoMapper {

    public SolicitacaoCreditoResponseDto toResponse(SolicitacaoCredito solicitacao) {
        if (solicitacao == null) {
            return null;
        }

        return SolicitacaoCreditoResponseDto.builder()
                .id(solicitacao.getId())
                .numeroCredito(solicitacao.getNumeroCredito())
                .numeroNfse(solicitacao.getNumeroNfse())
                .dataConstituicao(solicitacao.getDataConstituicao())
                .valorIssqn(solicitacao.getValorIssqn())
                .tipoCredito(solicitacao.getTipoCredito())
                .simplesNacional(solicitacao.isSimplesNacional() ? "Sim" : "Não")
                .aliquota(solicitacao.getAliquota())
                .valorFaturado(solicitacao.getValorFaturado())
                .valorDeducao(solicitacao.getValorDeducao())
                .baseCalculo(solicitacao.getBaseCalculo())
                .status(solicitacao.getStatus())
                .nomeSolicitante(solicitacao.getNomeSolicitante())
                .comprovanteUrl(solicitacao.getComprovanteUrl())
                .dataSolicitacao(solicitacao.getDataSolicitacao())
                .comentarioAnalise(solicitacao.getComentarioAnalise())
                .dataAnalise(solicitacao.getDataAnalise())
                .build();
    }
}




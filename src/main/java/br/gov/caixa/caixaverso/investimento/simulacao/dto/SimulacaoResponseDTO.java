package br.gov.caixa.caixaverso.investimento.simulacao.dto;

import br.gov.caixa.caixaverso.investimento.recomendacao.dto.ProdutoDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Resposta da simulação de investimento")
public record SimulacaoResponseDTO(
        @Schema(description = "Detalhes do produto de investimento validado para a simulação.")
        ProdutoDTO produtoValidado,

        @Schema(description = "O resultado financeiro da simulação.")
        ResultadoSimulacaoDTO resultadoSimulacao,

        @Schema(description = "Data e hora (UTC) em que a simulação foi realizada.")
        Instant dataSimulacao
) {
    @Schema(description = "Resultado financeiro detalhado")
    public record ResultadoSimulacaoDTO(
            @Schema(description = "Valor final bruto (sem impostos) após o prazo.", example = "11200.00")
            BigDecimal valorFinal,

            @Schema(description = "Rentabilidade anual do produto.", example = "0.12")
            BigDecimal rentabilidadeEfetiva,

            @Schema(description = "Prazo da simulação em meses.", example = "12")
            Integer prazoMeses
    ) {}
}

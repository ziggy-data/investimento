package br.gov.caixa.caixaverso.investimento.simulacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * DTO para representar a resposta agregada de simulações.
 * Usado pelo endpoint GET /simulacoes/por-produto-dia.
 */
@Schema(description = "Dados agregados de simulações por produto e dia ")
public record SimulacaoAgregadaDTO(
        @Schema(description = "Nome do produto", example = "CDB Caixa 2026")
        String produto,

        @Schema(description = "Data da agregação", example = "2025-10-30")
        LocalDate data,

        @Schema(description = "Número total de simulações para este produto neste dia", example = "15")
        Long quantidadeSimulacoes,

        @Schema(description = "Média do valor final simulado", example = "11050.00")
        BigDecimal mediaValorFinal
) {
    /**
     * Este construtor é usado especificamente pela consulta JPQL (Projeção).
     * O JPQL 'AVG' retorna Double, e nos convertemos para BigDecimal
     * com 2 casas decimais, garantindo a formatação correta.
     * * @param produto Nome do produto (do GROUP BY)
     * @param data Data da simulação (do GROUP BY)
     * @param quantidadeSimulacoes COUNT(s.id)
     * @param mediaValorFinal AVG(s.valorFinal) - vem como Double
     */
    public SimulacaoAgregadaDTO(String produto, LocalDate data, Long quantidadeSimulacoes, Double mediaValorFinal) {
        this(
                produto,
                data,
                quantidadeSimulacoes,
                // Converte o Double para BigDecimal com 2 casas e arredondamento
                mediaValorFinal == null ? BigDecimal.ZERO :
                        BigDecimal.valueOf(mediaValorFinal).setScale(2, RoundingMode.HALF_UP)
        );
    }
}
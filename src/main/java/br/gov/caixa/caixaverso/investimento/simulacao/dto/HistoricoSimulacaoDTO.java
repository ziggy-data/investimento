package br.gov.caixa.caixaverso.investimento.simulacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Representa um item no histórico de simulações realizadas")
public record HistoricoSimulacaoDTO(
        @Schema(description = "ID único da simulação", example = "1")
        Long id,

        @Schema(description = "ID do cliente que simulou", example = "123")
        Long clienteId,

        @Schema(description = "Nome do produto simulado", example = "CDB Caixa 2026")
        String produto,

        @Schema(description = "Valor original investido", example = "10000.00")
        BigDecimal valorInvestido,

        @Schema(description = "Valor final calculado na simulação", example = "11200.00")
        BigDecimal valorFinal,

        @Schema(description = "Prazo da simulação em meses", example = "12")
        Integer prazoMeses,

        @Schema(description = "Data e hora (UTC) em que a simulação foi feita")
        Instant dataSimulacao
) {}
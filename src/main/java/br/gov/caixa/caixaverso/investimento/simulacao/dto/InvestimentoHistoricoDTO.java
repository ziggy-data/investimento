package br.gov.caixa.caixaverso.investimento.simulacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para a resposta do endpoint GET /investimentos/{clienteId} .
 * Mapeado a partir da entidade Simulacao para este desafio.
 */
@Schema(description = "Representa um item do histórico de investimentos de um cliente")
public record InvestimentoHistoricoDTO(
        @Schema(description = "ID do investimento (baseado na simulação)", example = "1")
        Long id,

        @Schema(description = "Tipo do produto", example = "CDB")
        String tipo,

        @Schema(description = "Valor investido", example = "5000.00")
        BigDecimal valor,

        @Schema(description = "Rentabilidade anual do produto", example = "0.12")
        BigDecimal rentabilidade,

        @Schema(description = "Data do investimento", example = "2025-01-15")
        LocalDate data
) {}
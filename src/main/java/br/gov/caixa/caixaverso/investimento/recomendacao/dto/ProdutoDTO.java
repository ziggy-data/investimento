package br.gov.caixa.caixaverso.investimento.recomendacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * DTO reutilizável que representa um produto de investimento.
 * Usado na simulação e nas recomendações.
 */
@Schema(description = "Representa um produto de investimento")
public record ProdutoDTO(
        @Schema(description = "ID único do produto", example = "101")
        Long id,

        @Schema(description = "Nome comercial do produto", example = "CDB Caixa 2026")
        String nome,

        @Schema(description = "Tipo do produto (CDB, LCI, Fundo...)", example = "CDB")
        String tipo,

        @Schema(description = "Taxa de rentabilidade anual", example = "0.12")
        BigDecimal rentabilidade,

        @Schema(description = "Nível de risco (Baixo, Moderado, Alto)", example = "Baixo")
        String risco
) {}
package br.gov.caixa.caixaverso.investimento.recomendacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representa o perfil de risco calculado de um cliente ")
public record PerfilRiscoDTO(
        @Schema(description = "ID do cliente analisado", example = "123")
        Long clienteId,

        @Schema(description = "Perfil de risco calculado", example = "Moderado")
        String perfil,

        @Schema(description = "Pontuação final (0-100) usada para o cálculo", example = "65")
        int pontuacao,

        @Schema(description = "Descrição textual do perfil", example = "Perfil equilibrado entre segurança e rentabilidade.")
        String descricao
) {}
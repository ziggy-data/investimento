package br.gov.caixa.caixaverso.investimento.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta da autenticação contendo o token JWT.")
public record LoginResponse(
        @Schema(description = "Token JWT (Bearer Token)")
        String token
) {}
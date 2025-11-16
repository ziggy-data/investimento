package br.gov.caixa.caixaverso.investimento.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados de entrada para autenticação.")
public record LoginRequest(
        @NotBlank(message = "O nome do usuário não pode ser vazio")
        @Schema(description = "Nome do usuário (username)", example = "admin")
        String username,

        @NotBlank(message = "A senha não pode ser vazia")
        @Schema(description = "Senha do usuário", example = "password123")
        String password
) {}
package br.gov.caixa.caixaverso.investimento.simulacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Dados de entrada para solicitar uma simulação de investimento")
public record SimulacaoRequestDTO(

        @NotNull(message = "O ID do cliente não pode ser nulo.")
        @Schema(description = "ID único do cliente.", example = "123")
        Long clienteId,

        @NotNull(message = "O valor não pode ser nulo.")
        @DecimalMin(value = "100.00", message = "O valor mínimo para simulação é R$ 100,00.")
        @Schema(description = "Valor a ser investido.", example = "10000.00")
        BigDecimal valor,

        @NotNull(message = "O prazo não pode ser nulo.")
        @Min(value = 1, message = "O prazo mínimo é de 1 mês.")
        @Schema(description = "Prazo do investimento em meses.", example = "12")
        Integer prazoMeses,

        @NotBlank(message = "O tipo do produto não pode estar em branco.")
        @Schema(description = "Tipo do produto desejado (ex: CDB, LCI, Fundo).", example = "CDB")
        String tipoProduto
) {}

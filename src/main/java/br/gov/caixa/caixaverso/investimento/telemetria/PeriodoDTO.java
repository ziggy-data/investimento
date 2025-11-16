package br.gov.caixa.caixaverso.investimento.telemetria;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * DTO para o período da telemetria
 */
@Schema(description = "Período de tempo da coleta de métricas")
public record PeriodoDTO(
        @Schema(description = "Data de início da coleta", example = "2025-10-01")
        LocalDate inicio,

        @Schema(description = "Data de fim da coleta", example = "2025-10-31")
        LocalDate fim
) {}

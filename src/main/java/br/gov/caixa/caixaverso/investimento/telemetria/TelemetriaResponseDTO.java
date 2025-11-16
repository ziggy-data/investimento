package br.gov.caixa.caixaverso.investimento.telemetria;


import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO principal para a resposta do endpoint GET /telemetria.
 */
@Schema(description = "Resposta do endpoint de telemetria, contendo métricas da API")
public record TelemetriaResponseDTO(
        @Schema(description = "Lista de métricas por serviço (endpoint)")
        List<ServicoTelemetriaDTO> servicos,

        @Schema(description = "Período em que as métricas foram coletadas")
        PeriodoDTO periodo
) {}
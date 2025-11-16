package br.gov.caixa.caixaverso.investimento.telemetria.service;

import br.gov.caixa.caixaverso.investimento.telemetria.TelemetriaResponseDTO;

public interface TelemetriaService {
    /**
     * Coleta e formata os dados de telemetria da aplicação.
     * @return DTO com os dados de telemetria.
     */
    TelemetriaResponseDTO getDadosTelemetria();
}
package br.gov.caixa.caixaverso.investimento.telemetria;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Métricas de um serviço (endpoint) específico")
public record ServicoTelemetriaDTO(
        @Schema(description = "Nome lógico do serviço", example = "simular-investimento")
        String nome,

        @Schema(description = "Número total de chamadas ao endpoint", example = "120")
        long quantidadeChamadas,

        @Schema(description = "Tempo médio de resposta em milissegundos", example = "250")
        double mediaTempoRespostaMs
) {}
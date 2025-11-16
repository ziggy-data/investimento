package br.gov.caixa.caixaverso.investimento.telemetria.service;

import br.gov.caixa.caixaverso.investimento.telemetria.PeriodoDTO;
import br.gov.caixa.caixaverso.investimento.telemetria.ServicoTelemetriaDTO;
import br.gov.caixa.caixaverso.investimento.telemetria.TelemetriaResponseDTO;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
public class TelemetriaServiceImpl implements TelemetriaService {

    private final MeterRegistry meterRegistry;
    private final Instant applicationStartTime;

    public TelemetriaServiceImpl(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        // Salva a data/hora de início da aplicação para usar no "periodo"
        long startTimeMillis = ManagementFactory.getRuntimeMXBean().getStartTime();
        this.applicationStartTime = Instant.ofEpochMilli(startTimeMillis);
    }

    @Override
    public TelemetriaResponseDTO getDadosTelemetria() {

        // Este é o prefixo definido no seu Controller
        String prefixo = "/api/v1/investimentos";

        // Coleta métricas para todos os endpoints definidos no PDF.
        List<ServicoTelemetriaDTO> servicos = Stream.of(
                // O nome do serviço (do PDF) e o "URI template" (do seu Controller)
                getMetricasServico("simular-investimento", prefixo + "/simular"),
                getMetricasServico("simulacoes", prefixo + "/simulacoes"),
                getMetricasServico("simulacoes-por-produto-dia", prefixo + "/simulacoes/por-produto-dia"),

                // Os nomes dos endpoints do PDF
                getMetricasServico("perfil-risco", prefixo + "/perfil-risco/{clienteld}"),
                getMetricasServico("produtos-recomendados", prefixo + "/produtos-recomendados/{perfil}"),
                getMetricasServico("investimentos", prefixo + "/investimentos/{clienteld}")

        ).toList();

        // Esta parte (período) já estava correta
        PeriodoDTO periodo = new PeriodoDTO(
                LocalDate.ofInstant(applicationStartTime, ZoneId.systemDefault()),
                LocalDate.now(ZoneId.systemDefault())
        );

        return new TelemetriaResponseDTO(servicos, periodo);
    }

    /**
     * Helper para buscar as métricas de um URI específico no MeterRegistry.
     */
    private ServicoTelemetriaDTO getMetricasServico(String nome, String uriTemplate) {
        // Busca o "Timer" do Actuator pela tag de URI
        Timer timer = meterRegistry.find("http.server.requests")
                .tag("uri", uriTemplate)
                .timer();

        long count = 0;
        double avgMs = 0.0;

        if (timer != null) {
            count = timer.count();
            avgMs = timer.mean(TimeUnit.MILLISECONDS); // Pega a média já em Milissegundos
        }

        // Arredonda para 2 casas decimais para uma resposta limpa
        double mediaArredondada = Math.round(avgMs * 100.0) / 100.0;

        return new ServicoTelemetriaDTO(nome, count, mediaArredondada);
    }
}
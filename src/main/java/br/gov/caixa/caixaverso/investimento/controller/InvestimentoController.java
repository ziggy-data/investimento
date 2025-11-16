package br.gov.caixa.caixaverso.investimento.controller;

import br.gov.caixa.caixaverso.investimento.simulacao.dto.HistoricoSimulacaoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.InvestimentoHistoricoDTO;
import br.gov.caixa.caixaverso.investimento.recomendacao.dto.ProdutoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoAgregadaDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoRequestDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoResponseDTO;
import br.gov.caixa.caixaverso.investimento.recomendacao.dto.PerfilRiscoDTO;
import br.gov.caixa.caixaverso.investimento.telemetria.TelemetriaResponseDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.service.InvestimentoService;
import br.gov.caixa.caixaverso.investimento.recomendacao.service.MotorRecomendacaoService;
import br.gov.caixa.caixaverso.investimento.telemetria.service.TelemetriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/investimentos")
@Tag(name = "Investimentos", description = "Endpoints principais para simulação e consulta.")
public class InvestimentoController {

    private final InvestimentoService investimentoService;
    private final TelemetriaService telemetriaService;
    private final MotorRecomendacaoService motorRecomendacaoService;

    public InvestimentoController(InvestimentoService investimentoService,
                                  MotorRecomendacaoService motorRecomendacaoService,
                                  TelemetriaService telemetriaService) {
        this.investimentoService = investimentoService;
        this.motorRecomendacaoService = motorRecomendacaoService;
        this.telemetriaService = telemetriaService;
    }

    @Operation(summary = "Calcula, valida e salva uma nova simulação de investimento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulação calculada com sucesso",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SimulacaoResponseDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
    })
    @PostMapping("/simular")
    public ResponseEntity<SimulacaoResponseDTO> simularInvestimento(
            @Valid @RequestBody SimulacaoRequestDTO requestDTO
    ) {
        // A anotação @Valid dispara as validações no DTO.
        // Se falhar, o Exception Handler (abaixo) vai pegar.
        SimulacaoResponseDTO response = investimentoService.simularEValidar(requestDTO);

        // Retorna 200 OK com o JSON da simulação
        return ResponseEntity.ok(response);
    }

    /**
     * endpoint para GET /api/v1/investimentos/simulacoes
     *
     */
    @Operation(summary = "Busca o histórico de todas as simulações realizadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = HistoricoSimulacaoDTO.class)) }),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
    })
    @GetMapping("/simulacoes")
    public ResponseEntity<List<HistoricoSimulacaoDTO>> obterHistoricoSimulacoes() {
        List<HistoricoSimulacaoDTO> historico = investimentoService.buscarHistorico();
        return ResponseEntity.ok(historico);
    }

    /**
     * Retorna dados agregados por produto e dia.
     *
     */
    @Operation(summary = "Busca dados agregados (contagem e média) de simulações por dia e produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados agregados retornados com sucesso",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SimulacaoAgregadaDTO.class)) }),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
    })
    @GetMapping("/simulacoes/por-produto-dia")
    public ResponseEntity<List<SimulacaoAgregadaDTO>> obterSimulacoesPorProdutoDia() {
        List<SimulacaoAgregadaDTO> resultado = investimentoService.buscarSimulacoesAgregadas();
        return ResponseEntity.ok(resultado);
    }

    /**
     * Retorna dados de telemetria (métricas) da API.
     */
    @Operation(summary = "Retorna dados de telemetria (métricas) da API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas retornadas com sucesso",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TelemetriaResponseDTO.class)) }),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
    })
    @GetMapping("/telemetria")
    public ResponseEntity<TelemetriaResponseDTO> obterTelemetria() {
        TelemetriaResponseDTO dados = telemetriaService.getDadosTelemetria();
        return ResponseEntity.ok(dados);
    }

    /**
     * Retorna o perfil de risco calculado para um cliente.
     *
     * Usamos {clienteld} (com 'd') no path para bater com o template de URI
     * que o serviço de Telemetria já espera.
     */
    @Operation(summary = "Calcula o perfil de risco de um cliente com base no histórico ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil de risco calculado com sucesso",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PerfilRiscoDTO.class)) }),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Caminho não encontrado"),
    })
    @GetMapping("/perfil-risco/{clienteld}")
    public ResponseEntity<PerfilRiscoDTO> obterPerfilRisco(
            @Parameter(description = "ID do cliente a ser analisado.", example = "123", required = true)
            @PathVariable("clienteld") Long clienteId
    ) {
        PerfilRiscoDTO perfil = motorRecomendacaoService.calcularPerfilRisco(clienteId);
        return ResponseEntity.ok(perfil);
    }

    /**
     * Retorna produtos de investimento recomendados com base no perfil.
     */
    @Operation(summary = "Retorna uma lista de produtos recomendados para um perfil de risco")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produtos recomendados retornados com sucesso",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProdutoDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Perfil de risco inválido (ex: 'Iniciante')"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Caminho não encontrado")
    })
    @GetMapping("/produtos-recomendados/{perfil}")
    public ResponseEntity<List<ProdutoDTO>> obterProdutosRecomendados(
            @Parameter(description = "Perfil de risco (Conservador, Moderado, Agressivo).", example = "Moderado", required = true)
            @PathVariable String perfil
    ) {
        List<ProdutoDTO> produtos = motorRecomendacaoService.recomendarProdutos(perfil);
        return ResponseEntity.ok(produtos);
    }

    /**
     * Retorna o histórico de investimentos de um cliente.
     * O 'uri' aqui bate com o que o 'TelemetriaService' espera.
     */
    @Operation(summary = "Retorna o histórico de investimentos (baseado nas simulações) de um cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InvestimentoHistoricoDTO.class)) }),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Caminho não encontrado")
    })
    @GetMapping("/investimentos/{clienteld}")
    public ResponseEntity<List<InvestimentoHistoricoDTO>> obterHistoricoInvestimentos(
            @Parameter(description = "ID do cliente a ser consultado.", example = "123", required = true)
            @PathVariable("clienteld") Long clienteId
    ) {
        List<InvestimentoHistoricoDTO> historico = investimentoService.buscarHistoricoInvestimentos(clienteId);
        return ResponseEntity.ok(historico);
    }
}
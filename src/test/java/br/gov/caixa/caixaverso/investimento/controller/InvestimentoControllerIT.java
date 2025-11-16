package br.gov.caixa.caixaverso.investimento.controller;

import br.gov.caixa.caixaverso.investimento.auth.dto.LoginRequest;
import br.gov.caixa.caixaverso.investimento.auth.dto.LoginResponse;
import br.gov.caixa.caixaverso.investimento.simulacao.data.SimulacaoRepository;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
@DisplayName("Teste de Integração - InvestimentoController")
class InvestimentoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SimulacaoRepository simulacaoRepository; // Para setup e asserts

    private static final String BASE_URL = "/api/v1/investimentos";
    private String tokenCache; // Cache do token JWT para os testes

    /**
     * Helper para logar (uma vez) e obter um token JWT válido
     * para usar nos testes de endpoints protegidos.
     */
    @BeforeEach
    void setUp() throws Exception {
        if (tokenCache == null) {
            LoginRequest loginRequest = new LoginRequest("admin", "password123");
            MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            LoginResponse loginResponse = objectMapper.readValue(responseBody, LoginResponse.class);
            this.tokenCache = loginResponse.token();
        }
    }

    private String getAuthHeader() {
        return "Bearer " + this.tokenCache;
    }

    // --- Testes de Autenticação (Falhas Globais) ---
    @Nested
    @DisplayName("Falhas de Autenticação (401 Unauthorized)")
    class AutenticacaoTestes {
        @Test
        void simular_SemTokenJWT_DeveRetornar401() throws Exception {
            mockMvc.perform(post(BASE_URL + "/simular")).andExpect(status().isUnauthorized());
        }

        @Test
        void obterHistoricoSimulacoes_SemTokenJWT_DeveRetornar401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/simulacoes")).andExpect(status().isUnauthorized());
        }

        @Test
        void obterSimulacoesPorProdutoDia_SemTokenJWT_DeveRetornar401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/simulacoes/por-produto-dia")).andExpect(status().isUnauthorized());
        }

        @Test
        void obterTelemetria_SemTokenJWT_DeveRetornar401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/telemetria")).andExpect(status().isUnauthorized());
        }

        @Test
        void obterPerfilRisco_SemTokenJWT_DeveRetornar401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/perfil-risco/123")).andExpect(status().isUnauthorized());
        }

        @Test
        void obterProdutosRecomendados_SemTokenJWT_DeveRetornar401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/produtos-recomendados/Moderado")).andExpect(status().isUnauthorized());
        }

        @Test
        void obterHistoricoInvestimentos_SemTokenJWT_DeveRetornar401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/investimentos/123")).andExpect(status().isUnauthorized());
        }
    }

    // --- Teste de Rotas (404 Not Found) ---
    @Nested
    @DisplayName("Falhas de Rota (404 Not Found)")
    class RotaTestes {
        @Test
        void get_UrlInexistente_ComToken_DeveRetornar404() throws Exception {
            mockMvc.perform(get(BASE_URL + "/url-que-nao-existe")
                            .header("Authorization", getAuthHeader()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", is("Caminho Não Encontrado")));
        }
    }

    // --- Endpoint: POST /simular ---
    @Nested
    @DisplayName("POST /simular")
    class SimularTestes {

        @Test
        @DisplayName("Sucesso (PDF Caso 1): Deve simular CDB 10k/12m e retornar 200")
        void simular_PDF_CDB_10k_12m_DeveRetornar200() throws Exception {
            SimulacaoRequestDTO request = new SimulacaoRequestDTO(
                    123L, new BigDecimal("10000.00"), 12, "CDB");

            mockMvc.perform(post(BASE_URL + "/simular")
                            .header("Authorization", getAuthHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.produtoValidado.nome", is("CDB Caixa 2026")))
                    .andExpect(jsonPath("$.resultadoSimulacao.valorFinal", is(11200.00)));
        }

        @Test
        @DisplayName("Sucesso (PDF Caso 2): Deve simular Fundo 5k/6m e retornar 200")
        void simular_PDF_Fundo_5k_6m_DeveRetornar200() throws Exception {
            SimulacaoRequestDTO request = new SimulacaoRequestDTO(
                    123L, new BigDecimal("5000.00"), 6, "Fundo");

            mockMvc.perform(post(BASE_URL + "/simular")
                            .header("Authorization", getAuthHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.produtoValidado.nome", is("Fundo XPTO")))
                    .andExpect(jsonPath("$.resultadoSimulacao.valorFinal", is(5800.00)));
        }

        @Test
        @DisplayName("Falha (Validação 1): Deve retornar 400 para valor abaixo do mínimo (Valor Limite)")
        void simular_ComValorAbaixoDoMinimo_DeveRetornar400() throws Exception {
            SimulacaoRequestDTO request = new SimulacaoRequestDTO(
                    123L, new BigDecimal("50.00"), 12, "CDB");

            mockMvc.perform(post(BASE_URL + "/simular")
                            .header("Authorization", getAuthHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details[0]", containsString("O valor mínimo para simulação é R$ 100,00")));
        }

        @Test
        @DisplayName("Falha (Validação 2): Deve retornar 400 para prazo zero (Valor Limite)")
        void simular_ComPrazoZero_DeveRetornar400() throws Exception {
            SimulacaoRequestDTO request = new SimulacaoRequestDTO(
                    123L, new BigDecimal("1000.00"), 0, "CDB");

            mockMvc.perform(post(BASE_URL + "/simular")
                            .header("Authorization", getAuthHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details[0]", containsString("O prazo mínimo é de 1 mês")));
        }

        @Test
        @DisplayName("Falha (Validação 3): Deve retornar 400 para tipoProduto em branco")
        void simular_ComTipoProdutoEmBranco_DeveRetornar400() throws Exception {
            SimulacaoRequestDTO request = new SimulacaoRequestDTO(
                    123L, new BigDecimal("1000.00"), 12, "");

            mockMvc.perform(post(BASE_URL + "/simular")
                            .header("Authorization", getAuthHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details[0]", containsString("O tipo do produto não pode estar em branco")));
        }

        @Test
        @DisplayName("Falha (Validação 4): Deve retornar 400 para Body JSON vazio (Partição de Equivalência)")
        void simular_ComBodyVazio_DeveRetornar400() throws Exception {
            mockMvc.perform(post(BASE_URL + "/simular")
                            .header("Authorization", getAuthHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Erro de Validação")));
        }

        @Test
        @DisplayName("Falha (Regra de Negócio 1): Deve retornar 400 para produto inexistente")
        void simular_ComProdutoInexistente_DeveRetornar400() throws Exception {
            SimulacaoRequestDTO request = new SimulacaoRequestDTO(
                    123L, new BigDecimal("1000.00"), 12, "Poupança");

            mockMvc.perform(post(BASE_URL + "/simular")
                            .header("Authorization", getAuthHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest()) // 400 (do GlobalExceptionHandler)
                    .andExpect(jsonPath("$.message", is("Erro na Regra de Negócio")))
                    .andExpect(jsonPath("$.details[0]", containsString("Nenhum produto do tipo 'Poupança'")));
        }

        @Test
        @DisplayName("Falha (Regra de Negócio 2): Deve retornar 400 para valor insuficiente (LCI)")
        void simular_ComValorInsuficienteParaProduto_DeveRetornar400() throws Exception {
            // LCI (data.sql) tem valor mínimo de 1000.00
            SimulacaoRequestDTO request = new SimulacaoRequestDTO(
                    123L, new BigDecimal("500.00"), 12, "LCI");

            mockMvc.perform(post(BASE_URL + "/simular")
                            .header("Authorization", getAuthHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Erro na Regra de Negócio")));
        }

        @Test
        @DisplayName("Falha (Regra de Negócio 3): Deve retornar 400 para prazo insuficiente (LCI)")
        void simular_ComPrazoInsuficienteParaProduto_DeveRetornar400() throws Exception {
            // LCI 2027 (data.sql) tem prazo mínimo de 24 meses
            SimulacaoRequestDTO request = new SimulacaoRequestDTO(
                    123L, new BigDecimal("5000.00"), 12, "LCI");

            mockMvc.perform(post(BASE_URL + "/simular")
                            .header("Authorization", getAuthHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Erro na Regra de Negócio")));
        }
    }

    // --- Endpoint: GET /simulacoes ---
    @Nested
    @DisplayName("GET /simulacoes")
    class GetSimulacoesTestes {

        @BeforeEach
        void setUpNested() {
            simulacaoRepository.deleteAll();
        }

        @Test
        @DisplayName("Sucesso: Deve retornar 200 e lista vazia se nada foi simulado")
        void obterHistoricoSimulacoes_DeveRetornar200EListaVazia() throws Exception {
            // O @Transactional garante que o banco está limpo
            mockMvc.perform(get(BASE_URL + "/simulacoes")
                            .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0))); // Verifica se a lista está vazia
        }

        @Test
        @DisplayName("Sucesso: Deve retornar 200 e lista populada após uma simulação")
        void obterHistoricoSimulacoes_AposSimulacao_DeveRetornarListaPopulada() throws Exception {
            // Cenário (Arrange) - (Integração POST -> GET)
            SimulacaoRequestDTO request = new SimulacaoRequestDTO(
                    123L, new BigDecimal("10000.00"), 12, "CDB");
            mockMvc.perform(post(BASE_URL + "/simular")
                            .header("Authorization", getAuthHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Ação (Act)
            mockMvc.perform(get(BASE_URL + "/simulacoes")
                            .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1))) // Agora a lista tem 1 item
                    .andExpect(jsonPath("$[0].produto", is("CDB Caixa 2026")));
        }
    }

    // --- Endpoint: GET /simulacoes/por-produto-dia ---
    @Nested
    @DisplayName("GET /simulacoes/por-produto-dia")
    class GetSimulacoesAgregadasTestes {

        @BeforeEach
        void setUpNested() {
            simulacaoRepository.deleteAll();
        }

        @Test
        @DisplayName("Sucesso: Deve retornar 200 e agregação correta após simulações")
        void obterSimulacoesPorProdutoDia_AposSimulacoes_DeveRetornarAgregado() throws Exception {
            // Cenário (Arrange): Simula 2 CDBs e 1 Fundo
            SimulacaoRequestDTO cdb1 = new SimulacaoRequestDTO(123L, new BigDecimal("1000.00"), 12, "CDB");
            SimulacaoRequestDTO cdb2 = new SimulacaoRequestDTO(123L, new BigDecimal("1000.00"), 12, "CDB");
            SimulacaoRequestDTO fundo1 = new SimulacaoRequestDTO(123L, new BigDecimal("5000.00"), 6, "Fundo");

            mockMvc.perform(post(BASE_URL + "/simular").header("Authorization", getAuthHeader()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cdb1)));
            mockMvc.perform(post(BASE_URL + "/simular").header("Authorization", getAuthHeader()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cdb2)));
            mockMvc.perform(post(BASE_URL + "/simular").header("Authorization", getAuthHeader()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(fundo1)));

            // Ação (Act)
            mockMvc.perform(get(BASE_URL + "/simulacoes/por-produto-dia")
                            .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2))) // 2 grupos (CDB e Fundo)
                    .andExpect(jsonPath("$[?(@.produto == 'CDB Caixa 2026')].quantidadeSimulacoes", contains(2)))
                    .andExpect(jsonPath("$[?(@.produto == 'Fundo XPTO')].quantidadeSimulacoes", contains(1)))
                    .andExpect(jsonPath("$[?(@.produto == 'Fundo XPTO')].mediaValorFinal", contains(5800.00)));
        }
    }

    // --- Endpoint: GET /telemetria ---
    @Nested
    @DisplayName("GET /telemetria")
    class GetTelemetriaTestes {

        @Test
        @DisplayName("Sucesso: Deve retornar 200 e contagem 0 para API limpa")
        void obterTelemetria_DeveRetornar200EContagemZero() throws Exception {
            mockMvc.perform(get(BASE_URL + "/telemetria")
                            .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.servicos[?(@.nome == 'simular-investimento')]['quantidadeChamadas']", contains(0)));
        }

        // --- Endpoint: GET /perfil-risco/{clienteld} ---
        @Nested
        @DisplayName("GET /perfil-risco/{clienteld}")
        class GetPerfilRiscoTestes {

            @BeforeEach
            void setUpNested() {
                simulacaoRepository.deleteAll();
            }

            @Test
            @DisplayName("Sucesso (Partição): Deve retornar Conservador para cliente novo (ID 999)")
            void obterPerfilRisco_ParaClienteNovo_DeveRetornarConservador() throws Exception {
                mockMvc.perform(get(BASE_URL + "/perfil-risco/999")
                                .header("Authorization", getAuthHeader()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.perfil", is("Conservador")))
                        .andExpect(jsonPath("$.pontuacao", is(0)));
            }

            @Test
            @DisplayName("Sucesso (Regra): Deve retornar Agressivo após simulações de alto risco")
            void obterPerfilRisco_AposSimulacoesRiscoAlto_DeveRetornarAgressivo() throws Exception {
                // Cenário (Arrange): 11 simulações (Freq=33) de Alto Risco (Risco=34) e Valor Alto (Volume=33)
                SimulacaoRequestDTO request = new SimulacaoRequestDTO(
                        1L, new BigDecimal("50000.00"), 6, "Fundo"); // Fundo XPTO (Alto)

                for (int i = 0; i < 11; i++) {
                    mockMvc.perform(post(BASE_URL + "/simular").header("Authorization", getAuthHeader()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)));
                }

                // Ação (Act)
                mockMvc.perform(get(BASE_URL + "/perfil-risco/1")
                                .header("Authorization", getAuthHeader()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.perfil", is("Agressivo")))
                        .andExpect(jsonPath("$.pontuacao", is(100)));
            }

            @Test
            @DisplayName("Falha (Validação): Deve retornar 400 para ID de cliente inválido")
            void obterPerfilRisco_ComIdClienteInvalido_DeveRetornar400() throws Exception {
                mockMvc.perform(get(BASE_URL + "/perfil-risco/abc")
                                .header("Authorization", getAuthHeader()))
                        .andExpect(status().isBadRequest()); // Erro de conversão de tipo (String "abc" para Long)
            }
        }

        // --- Endpoint: GET /produtos-recomendados/{perfil} ---
        @Nested
        @DisplayName("GET /produtos-recomendados/{perfil}")
        class GetProdutosRecomendadosTestes {

            @Test
            @DisplayName("Sucesso (Regra): Deve retornar apenas Risco Baixo para 'Conservador'")
            void obterProdutosRecomendados_PerfilConservador_DeveRetornarApenasRiscoBaixo() throws Exception {
                mockMvc.perform(get(BASE_URL + "/produtos-recomendados/Conservador")
                                .header("Authorization", getAuthHeader()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", everyItem( // Verifica CADA item da lista
                                hasKey("risco")
                        )))
                        .andExpect(jsonPath("$[?(@.risco != 'Baixo')]", empty())); // Garante que a lista de (risco != Baixo) é vazia
            }

            @Test
            @DisplayName("Sucesso (Regra): Deve retornar Risco Alto para 'Agressivo'")
            void obterProdutosRecomendados_PerfilAgressivo_DeveRetornarRiscoAlto() throws Exception {
                mockMvc.perform(get(BASE_URL + "/produtos-recomendados/Agressivo")
                                .header("Authorization", getAuthHeader()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[?(@.risco == 'Alto')]", not(empty()))); // Garante que a lista (risco == Alto) NÃO é vazia
            }

            @Test
            @DisplayName("Sucesso (Regra): Deve funcionar com case misturado (mOdErAdO)")
            void obterProdutosRecomendados_PerfilComCaseErrado_DeveFuncionar() throws Exception {
                mockMvc.perform(get(BASE_URL + "/produtos-recomendados/mOdErAdO")
                                .header("Authorization", getAuthHeader()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", not(empty()))); // Apenas verifica se funcionou
            }

            @Test
            @DisplayName("Falha (Regra de Negócio): Deve retornar 400 para perfil inválido")
            void obterProdutosRecomendados_PerfilInvalido_DeveRetornar400() throws Exception {
                mockMvc.perform(get(BASE_URL + "/produtos-recomendados/iniciante")
                                .header("Authorization", getAuthHeader()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message", is("Erro na Regra de Negócio")))
                        .andExpect(jsonPath("$.details[0]", containsString("Perfil de risco inválido: 'iniciante'")));
            }
        }

        // --- Endpoint: GET /investimentos/{clienteld} ---
        @Nested
        @DisplayName("GET /investimentos/{clienteld}")
        class GetInvestimentosTestes {

            @BeforeEach
            void setUpNested() {
                simulacaoRepository.deleteAll();
            }

            @Test
            @DisplayName("Sucesso: Deve retornar 200 e lista vazia para cliente novo")
            void obterHistoricoInvestimentos_ClienteNovo_DeveRetornar200EListaVazia() throws Exception {
                mockMvc.perform(get(BASE_URL + "/investimentos/999")
                                .header("Authorization", getAuthHeader()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(0)));
            }

            @Test
            @DisplayName("Sucesso: Deve retornar 200 e lista populada após simulação")
            void obterHistoricoInvestimentos_AposSimulacao_DeveRetornarListaPopulada() throws Exception {
                // Cenário (Arrange)
                SimulacaoRequestDTO request = new SimulacaoRequestDTO(
                        777L, new BigDecimal("1000.00"), 12, "CDB");
                mockMvc.perform(post(BASE_URL + "/simular").header("Authorization", getAuthHeader()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)));

                // Ação (Act)
                mockMvc.perform(get(BASE_URL + "/investimentos/777")
                                .header("Authorization", getAuthHeader()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(1)))
                        .andExpect(jsonPath("$[0].tipo", is("CDB")))
                        .andExpect(jsonPath("$[0].valor", is(1000.00)));
            }
        }
    }
}

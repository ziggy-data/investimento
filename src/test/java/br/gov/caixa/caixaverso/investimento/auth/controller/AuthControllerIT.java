package br.gov.caixa.caixaverso.investimento.auth.controller;

import br.gov.caixa.caixaverso.investimento.auth.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste de Integração para o AuthController.
 * Carrega o contexto completo do Spring e testa o endpoint /login.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional // Garante que o UserSeeder rode mas dê rollback
@DisplayName("Teste de Integração - AuthController")
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc; // Para enviar requisições HTTP falsas

    @Autowired
    private ObjectMapper objectMapper; // Para converter objetos em JSON

    private final String LOGIN_URL = "/api/v1/auth/login";

    // --- Testes de Sucesso ---

    @Test
    @DisplayName("Deve retornar 200 OK e um Token JWT para credenciais válidas")
    void login_ComCredenciaisValidas_DeveRetornar200EToken() throws Exception {
        // Cenário (Arrange) - (Regra de Negócio: Sucesso)
        // O UserSeeder já criou o usuário "admin" com senha "password123"
        LoginRequest request = new LoginRequest("admin", "password123");

        // Ação (Act) & Verificação (Assert)
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // HTTP 200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", notNullValue())); // Verifica se o token existe
    }

    // --- Testes de Falha (Regras de Negócio - Autenticação) ---

    @Test
    @DisplayName("Deve retornar 401 Unauthorized para senha incorreta")
    void login_ComSenhaIncorreta_DeveRetornar401() throws Exception {
        // Cenário (Arrange) - (Partição de Equivalência: Credenciais Inválidas)
        LoginRequest request = new LoginRequest("admin", "senha-errada");

        // Ação (Act) & Verificação (Assert)
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()) // HTTP 401 (do GlobalExceptionHandler)
                .andExpect(jsonPath("$.message", is("Autenticação Falhou")))
                .andExpect(jsonPath("$.details[0]", is("Usuário ou senha inválidos.")));
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized para usuário inexistente")
    void login_ComUsuarioInexistente_DeveRetornar401() throws Exception {
        // Cenário (Arrange) - (Partição de Equivalência: Credenciais Inválidas)
        LoginRequest request = new LoginRequest("usuario-fantasma", "password123");

        // Ação (Act) & Verificação (Assert)
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()) // HTTP 401
                .andExpect(jsonPath("$.message", is("Autenticação Falhou")));
    }

    // --- Testes de Falha (Validação de Entrada - Dados Incorretos) ---

    @Test
    @DisplayName("Deve retornar 400 Bad Request para username vazio")
    void login_ComUsernameVazio_DeveRetornar400() throws Exception {
        // Cenário (Arrange) - (Valor Limite: String vazia)
        LoginRequest request = new LoginRequest("", "password123");

        // Ação (Act) & Verificação (Assert)
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // HTTP 400 (do @Valid)
                .andExpect(jsonPath("$.message", is("Erro de Validação")))
                .andExpect(jsonPath("$.details[0]", is("username: O nome do usuário não pode ser vazio")));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request para password nulo (campo ausente)")
    void login_ComPasswordNulo_DeveRetornar400() throws Exception {
        // Cenário (Arrange) - (Partição de Equivalência: Campo ausente)
        // Criamos um JSON "malformado"
        String jsonRequest = "{\"username\": \"admin\"}";

        // Ação (Act) & Verificação (Assert)
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest()) // HTTP 400
                .andExpect(jsonPath("$.message", is("Erro de Validação")))
                .andExpect(jsonPath("$.details[0]", is("password: A senha não pode ser vazia")));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request para body JSON vazio")
    void login_ComBodyVazio_DeveRetornar400() throws Exception {
        // Cenário (Arrange) - (Valor Limite: Objeto vazio)
        String jsonRequest = "{}";

        // Ação (Act) & Verificação (Assert)
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest()) // HTTP 400
                .andExpect(jsonPath("$.message", is("Erro de Validação")));
    }
}
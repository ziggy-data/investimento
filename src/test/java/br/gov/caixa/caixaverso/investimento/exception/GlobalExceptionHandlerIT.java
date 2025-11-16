package br.gov.caixa.caixaverso.investimento.exception;

import br.gov.caixa.caixaverso.investimento.auth.dto.LoginRequest;
import br.gov.caixa.caixaverso.investimento.auth.dto.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de Integração focado no GlobalExceptionHandler.
 * Carrega o contexto completo do Spring e usa um Controller de Teste
 * para forçar exceções específicas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Teste de Integração - GlobalExceptionHandler")
@Import(GlobalExceptionHandlerIT.TestControllerConfiguration.class)
class GlobalExceptionHandlerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenCache; // Token JWT para os testes

    /**
     * Configuração especial (somente para este teste) que cria
     * um endpoint falso para podermos forçar um erro 500.
     */
    @TestConfiguration
    static class TestControllerConfiguration {
        @RestController
        public static class Test500Controller {

            // Este endpoint protegido força um NullPointerException
            @GetMapping("/api/v1/test/500")
            public String throwGeneric500Error() {
                // Lança uma exceção genérica que não é tratada
                // por nenhum outro handler específico.
                throw new NullPointerException("Erro 500 genérico de teste");
            }

            // Este endpoint testa o MethodArgumentTypeMismatchException
            @GetMapping("/api/v1/test/mismatch/{id}")
            public String throwTypeMismatch(@PathVariable Integer id) {
                return "OK";
            }
        }
    }

    /**
     * Obtém um token JWT válido antes de cada teste
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

    // --- O TESTE QUE FALTAVA ---

    @Test
    @DisplayName("Deve capturar exceção genérica (NullPointerException) e retornar 500")
    void handleGenericExceptions_QuandoNullPointerExceptionOcorre_DeveRetornar500() throws Exception {
        // Ação (Act)
        // Chama o endpoint de teste que força um NullPointerException
        mockMvc.perform(get("/api/v1/test/500")
                        .header("Authorization", "Bearer " + tokenCache))

                // Verificação (Assert)
                .andExpect(status().isInternalServerError()) // HTTP 500
                .andExpect(jsonPath("$.message", is("Erro Interno do Servidor")))
                .andExpect(jsonPath("$.details[0]", is("Ocorreu um erro inesperado.")));
    }

    // --- Testes de Cobertura Adicionais (Verificando os outros handlers) ---

    @Test
    @DisplayName("Deve capturar MethodArgumentTypeMismatchException e retornar 400")
    void handleTypeMismatchException_QuandoPathVarTemTipoErrado_DeveRetornar400() throws Exception {
        // Ação (Act)
        // Chama o endpoint /test/mismatch/{id} (que espera um Integer)
        // com um valor String ("abc").
        mockMvc.perform(get("/api/v1/test/mismatch/abc")
                        .header("Authorization", "Bearer " + tokenCache))

                // Verificação (Assert)
                .andExpect(status().isBadRequest()) // HTTP 400
                .andExpect(jsonPath("$.message", is("Tipo de Parâmetro Inválido")))
                .andExpect(jsonPath("$.details[0]",
                        is("O parâmetro de URL 'id' está no formato errado. Era esperado um valor do tipo 'Integer', mas foi recebido: 'abc'.")));
    }
}
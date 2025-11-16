package br.gov.caixa.caixaverso.investimento.recomendacao.domain;

import br.gov.caixa.caixaverso.investimento.exception.ValidacaoNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Teste Unitário - Domínio PerfilRisco")
class PerfilRiscoTest {

    // --- Testes de Sucesso (Regra de Negócio) ---

    @ParameterizedTest(name = "fromPontuacao: Pontuação {0} deve retornar {1}")
    @CsvSource({
            "0, CONSERVADOR",   // Limite inferior
            "40, CONSERVADOR",  // Limite superior do Conservador
            "41, MODERADO",     // Limite inferior do Moderado
            "70, MODERADO",     // Limite superior do Moderado
            "71, AGRESSIVO",    // Limite inferior do Agressivo
            "100, AGRESSIVO",   // Limite superior
            "-10, CONSERVADOR"  // Caso de pontuação negativa
    })
    @DisplayName("Deve mapear corretamente a pontuação para o perfil de risco")
    void testFromPontuacao_DeveRetornarPerfilCorreto_ParaCadaFaixaDePontuacao(int pontuacao, PerfilRisco esperado) {
        // Ação
        PerfilRisco resultado = PerfilRisco.fromPontuacao(pontuacao);

        // Verificação
        assertEquals(esperado, resultado);
    }

    @ParameterizedTest(name = "fromString: Input \"{0}\" deve retornar {1}")
    @CsvSource({
            "Conservador, CONSERVADOR",
            "moderado, MODERADO",        // Testando case insensitive
            " AGRESSIVO , AGRESSIVO"    // Testando com espaços (trim)
    })
    @DisplayName("Deve mapear corretamente a string de nome para o Enum")
    void testFromString_DeveRetornarPerfilCorreto_IgnorandoCaseEEspacos(String input, PerfilRisco esperado) {
        // Ação
        PerfilRisco resultado = PerfilRisco.fromString(input);

        // Verificação
        assertEquals(esperado, resultado);
    }

    // --- Testes de Falha (Dados Incorretos) ---

    @ParameterizedTest(name = "fromString: Input \"{0}\" deve lançar exceção")
    @NullAndEmptySource // Testa com null e "" (vazio)
    @ValueSource(strings = {"  "}) // Testa com " " (branco)
    @DisplayName("Deve lançar ValidacaoNegocioException para input nulo, vazio ou em branco")
    void testFromString_DeveLancarExcecao_QuandoInputForNuloOuVazio(String input) {
        // Verificação
        // Verifica se a exceção ValidacaoNegocioException é lançada
        var excecao = assertThrows(ValidacaoNegocioException.class, () -> {
            // Ação
            PerfilRisco.fromString(input);
        });

        // (Opcional) Verifica a mensagem da exceção
        if (input == null) {
            assertEquals("Perfil de risco não pode ser nulo.", excecao.getMessage());
        }
    }

    @Test
    @DisplayName("Deve lançar ValidacaoNegocioException para string de perfil inválida")
    void testFromString_DeveLancarExcecao_QuandoInputForInvalido() {
        // Cenário
        String inputInvalido = "Iniciante";

        // Verificação
        var excecao = assertThrows(ValidacaoNegocioException.class, () -> {
            // Ação
            PerfilRisco.fromString(inputInvalido);
        });

        // Verificação da mensagem
        assertTrue(excecao.getMessage().contains("Perfil de risco inválido: 'Iniciante'"));
    }
}
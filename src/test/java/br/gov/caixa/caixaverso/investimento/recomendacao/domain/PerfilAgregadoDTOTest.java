package br.gov.caixa.caixaverso.investimento.recomendacao.domain;

import br.gov.caixa.caixaverso.investimento.recomendacao.dto.PerfilAgregadoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste Unitário para o DTO PerfilAgregadoDTO.
 * Focado em validar a lógica de defesa (null-check) no construtor compacto.
 */
@DisplayName("Teste Unitário - DTO PerfilAgregado")
class PerfilAgregadoDTOTest {

    // --- Teste de Sucesso (Caminho Feliz) ---

    @Test
    @DisplayName("Deve atribuir valores corretamente quando a contagem NÃO é nula")
    void construtor_DeveAtribuirValores_QuandoContagemNaoENula() {
        // Cenário (Arrange)
        Long contagem = 10L;
        Double mediaValor = 5000.0;
        Double mediaRisco = 20.0;

        // Ação (Act)
        PerfilAgregadoDTO dto = new PerfilAgregadoDTO(contagem, mediaValor, mediaRisco);

        // Verificação (Assert)
        // Verifica se os valores foram simplesmente repassados
        assertEquals(10L, dto.contagem());
        assertEquals(5000.0, dto.mediaValor());
        assertEquals(20.0, dto.mediaRisco());
    }

    // --- Teste de Falha / Regra de Negócio (O Teste Importante) ---

    @Test
    @DisplayName("Deve converter a contagem nula para 0L no construtor")
    void construtor_DeveConverterContagemNulaParaZero() {
        // Cenário (Arrange)
        // Este é o caso que o seu teste de integração (SimulacaoRepositoryIT)
        // já cobre quando um clienteId não tem simulações.
        Long contagemNula = null;
        Double mediaValorNula = null;
        Double mediaRiscoNula = null;

        // Ação (Act)
        // Chamamos o construtor com 'contagem' nula
        PerfilAgregadoDTO dto = new PerfilAgregadoDTO(contagemNula, mediaValorNula, mediaRiscoNula);

        // Verificação (Assert)
        // Verificamos se a lógica "if (contagem == null)" foi disparada
        assertNotNull(dto.contagem());
        assertEquals(0L, dto.contagem()); // A contagem DEVE ser 0, e não null
        assertNull(dto.mediaValor());
        assertNull(dto.mediaRisco());
    }
}
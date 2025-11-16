package br.gov.caixa.caixaverso.investimento.simulacao.mapper;

import br.gov.caixa.caixaverso.investimento.recomendacao.dto.ProdutoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.domain.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Teste Unitário para o ProdutoMapper.
 * Focado em testar a lógica de transformação de Entidade para DTO.
 */
@ExtendWith(MockitoExtension.class) // Habilita o Mockito
@DisplayName("Teste Unitário - ProdutoMapper")
class ProdutoMapperTest {

    private ProdutoMapper produtoMapper;

    @BeforeEach
    void setUp() {
        // Instancia o mapper. Não precisamos de @InjectMocks
        // porque ele não tem dependências.
        produtoMapper = new ProdutoMapper();
    }

    // --- Teste de Sucesso ---

    @Test
    @DisplayName("Deve mapear corretamente a Entidade Produto para ProdutoDTO")
    void toDTO_DeveMapearCorretamente_QuandoProdutoForValido() {
        // Cenário (Arrange)
        // Criamos um mock da entidade 'Produto' para isolar o teste
        Produto mockEntidade = mock(Produto.class);

        // Configuramos o que o mock deve retornar quando seus getters forem chamados
        when(mockEntidade.getId()).thenReturn(101L);
        when(mockEntidade.getNome()).thenReturn("CDB Caixa 2026");
        when(mockEntidade.getTipo()).thenReturn("CDB");
        when(mockEntidade.getRentabilidadeAnual()).thenReturn(new BigDecimal("0.12"));
        when(mockEntidade.getRisco()).thenReturn("Baixo");

        // Ação (Act)
        ProdutoDTO dto = produtoMapper.toDTO(mockEntidade);

        // Verificação (Assert)
        // Verificamos se cada campo do DTO corresponde ao da entidade
        assertNotNull(dto);
        assertEquals(101L, dto.id());
        assertEquals("CDB Caixa 2026", dto.nome());
        assertEquals("CDB", dto.tipo());
        assertEquals(new BigDecimal("0.12"), dto.rentabilidade());
        assertEquals("Baixo", dto.risco());
    }

    // --- Teste de Falha (Dados Incorretos / Nulos) ---

    @Test
    @DisplayName("Deve retornar null quando a Entidade Produto for nula")
    void toDTO_DeveRetornarNull_QuandoProdutoForNulo() {
        // Cenário (Arrange)
        Produto produtoNulo = null;

        // Ação (Act)
        ProdutoDTO dto = produtoMapper.toDTO(produtoNulo);

        // Verificação (Assert)
        assertNull(dto, "O DTO deveria ser nulo quando a entidade de entrada é nula.");
    }
}
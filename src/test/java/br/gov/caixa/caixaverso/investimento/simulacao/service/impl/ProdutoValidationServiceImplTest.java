package br.gov.caixa.caixaverso.investimento.simulacao.service.impl;

import br.gov.caixa.caixaverso.investimento.exception.ValidacaoNegocioException;
import br.gov.caixa.caixaverso.investimento.simulacao.data.ProdutoRepository;
import br.gov.caixa.caixaverso.investimento.simulacao.domain.Produto;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste Unitário - ProdutoValidationService (@Cacheable)")
class ProdutoValidationServiceImplTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoValidationServiceImpl produtoValidationService;

    // --- Testes de Sucesso (Regras de Negócio) ---

    @Test
    @DisplayName("Deve retornar o produto quando o repositório encontrar")
    void validarProduto_DeveRetornarProduto_QuandoRepositorioEncontrar() {
        // Cenário (Arrange)
        SimulacaoRequestDTO request = new SimulacaoRequestDTO(
                1L, new BigDecimal("500.00"), 6, "CDB"
        );
        Produto mockProduto = mock(Produto.class);

        // Simula a chamada de repositório que a implementação REAL faz
        when(produtoRepository.findFirstByTipoAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeAnualDesc(
                eq(request.tipoProduto()),
                eq(request.valor()),
                eq(request.prazoMeses())
        )).thenReturn(Optional.of(mockProduto));

        // Ação (Act)
        Produto produtoValidado = produtoValidationService.validarProduto(request);

        // Verificação (Assert)
        assertNotNull(produtoValidado);
        assertEquals(mockProduto, produtoValidado);
    }

    /**
     * Nota: O teste 'DeveRetornarMelhorRentabilidade' é agora um teste de
     * integração do Repositório. O Service apenas retorna o que o
     * repositório encontra, então o teste acima já cobre o sucesso.
     */

    @Test
    @DisplayName("Deve chamar o repositório com o tipo em minúsculas (cdb)")
    void validarProduto_DeveChamarRepositorio_ComStringExata() {
        // Cenário
        SimulacaoRequestDTO request = new SimulacaoRequestDTO(
                1L, new BigDecimal("2000.00"), 12, "cdb" // Tipo em minúsculas
        );
        Produto mockProduto = mock(Produto.class);

        // Simula a chamada DESTE request específico
        when(produtoRepository.findFirstByTipoAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeAnualDesc(
                eq("cdb"), // Espera "cdb" minúsculo
                eq(request.valor()),
                eq(request.prazoMeses())
        )).thenReturn(Optional.of(mockProduto));

        // Ação
        Produto produtoValidado = produtoValidationService.validarProduto(request);

        // Verificação
        assertNotNull(produtoValidado);
        assertEquals(mockProduto, produtoValidado);
    }

    // --- Testes de Falha (Regras de Negócio / Dados Incorretos) ---

    @Test
    @DisplayName("Deve lançar ValidacaoNegocioException se o repositório retornar Optional.empty()")
    void validarProduto_DeveLancarExcecao_QuandoRepositorioNaoEncontrar() {
        // Cenário (Falha: Dados Incorretos)
        SimulacaoRequestDTO request = new SimulacaoRequestDTO(
                1L, new BigDecimal("1000.00"), 12, "LCI" // "LCI" não será encontrado
        );

        // Simula o repositório não encontrando nada
        when(produtoRepository.findFirstByTipoAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeAnualDesc(
                any(), any(), any()
        )).thenReturn(Optional.empty());

        // Ação & Verificação
        var excecao = assertThrows(ValidacaoNegocioException.class, () -> {
            produtoValidationService.validarProduto(request);
        });

        assertTrue(excecao.getMessage().contains("Nenhum produto do tipo 'LCI' encontrado"));
    }
}
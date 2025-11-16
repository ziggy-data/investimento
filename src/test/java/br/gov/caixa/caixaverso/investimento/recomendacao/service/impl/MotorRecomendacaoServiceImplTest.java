package br.gov.caixa.caixaverso.investimento.recomendacao.service.impl;

import br.gov.caixa.caixaverso.investimento.exception.ValidacaoNegocioException;
import br.gov.caixa.caixaverso.investimento.recomendacao.dto.PerfilAgregadoDTO;
import br.gov.caixa.caixaverso.investimento.recomendacao.dto.PerfilRiscoDTO;
import br.gov.caixa.caixaverso.investimento.recomendacao.dto.ProdutoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.data.ProdutoRepository;
import br.gov.caixa.caixaverso.investimento.simulacao.data.SimulacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste Unitário - MotorRecomendacaoService")
class MotorRecomendacaoServiceImplTest {

    // Dependências que serão MOCKADAS (simuladas)
    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private SimulacaoRepository simulacaoRepository;

    // A classe que estamos testando. Os Mocks acima serão injetados nela.
    @InjectMocks
    private MotorRecomendacaoServiceImpl motorRecomendacaoService;

    // --- Testes para calcularPerfilRisco ---

    @Test
    @DisplayName("Deve calcular Perfil AGRESSIVO para pontuação alta")
    void calcularPerfilRisco_DeveRetornarAgressivo_QuandoPontuacaoForAlta() {
        // Cenário (Arrange)
        Long clienteId = 1L;
        // Simulamos uma resposta do banco com alta frequência, valor e risco
        // Pontos: Freq(>10) = 33, Volume(>10k) = 33, Risco(Alto) = 34. Total = 100
        PerfilAgregadoDTO mockAgregado = new PerfilAgregadoDTO(20L, 15000.0, 34.0);
        when(simulacaoRepository.findPerfilAgregadoPorCliente(clienteId)).thenReturn(mockAgregado);

        // Ação (Act)
        PerfilRiscoDTO resultado = motorRecomendacaoService.calcularPerfilRisco(clienteId);

        // Verificação (Assert)
        assertNotNull(resultado);
        assertEquals(100, resultado.pontuacao());
        assertEquals("Agressivo", resultado.perfil());
    }

    @Test
    @DisplayName("Deve calcular Perfil MODERADO para pontuação média")
    void calcularPerfilRisco_DeveRetornarModerado_QuandoPontuacaoForMedia() {
        // Cenário (Arrange)
        Long clienteId = 2L;
        // Pontos: Freq(5) = 20, Volume(6k) = 20, Risco(Moderado) = 20. Total = 60
        PerfilAgregadoDTO mockAgregado = new PerfilAgregadoDTO(5L, 6000.0, 20.0);
        when(simulacaoRepository.findPerfilAgregadoPorCliente(clienteId)).thenReturn(mockAgregado);

        // Ação (Act)
        PerfilRiscoDTO resultado = motorRecomendacaoService.calcularPerfilRisco(clienteId);

        // Verificação (Assert)
        assertNotNull(resultado);
        assertEquals(60, resultado.pontuacao());
        assertEquals("Moderado", resultado.perfil());
    }

    @Test
    @DisplayName("Deve calcular Perfil CONSERVADOR para pontuação baixa")
    void calcularPerfilRisco_DeveRetornarConservador_QuandoPontuacaoForBaixa() {
        // Cenário (Arrange)
        Long clienteId = 3L;
        // Pontos: Freq(2) = 10, Volume(1k) = 10, Risco(Baixo) = 10. Total = 30
        PerfilAgregadoDTO mockAgregado = new PerfilAgregadoDTO(2L, 1000.0, 10.0);
        when(simulacaoRepository.findPerfilAgregadoPorCliente(clienteId)).thenReturn(mockAgregado);

        // Ação (Act)
        PerfilRiscoDTO resultado = motorRecomendacaoService.calcularPerfilRisco(clienteId);

        // Verificação (Assert)
        assertNotNull(resultado);
        assertEquals(30, resultado.pontuacao());
        assertEquals("Conservador", resultado.perfil());
    }

    @Test
    @DisplayName("Deve retornar Perfil CONSERVADOR (padrão) para cliente sem simulações")
    void calcularPerfilRisco_DeveRetornarConservadorPadrao_QuandoNaoHaSimulacoes() {
        // Cenário (Arrange) - (Falha: Dados incorretos/vazios)
        Long clienteId = 4L;
        // Simulamos o banco retornando 0 simulações
        PerfilAgregadoDTO mockVazio = new PerfilAgregadoDTO(0L, null, null);
        when(simulacaoRepository.findPerfilAgregadoPorCliente(clienteId)).thenReturn(mockVazio);

        // Ação (Act)
        PerfilRiscoDTO resultado = motorRecomendacaoService.calcularPerfilRisco(clienteId);

        // Verificação (Assert)
        assertNotNull(resultado);
        assertEquals(0, resultado.pontuacao());
        assertEquals("Conservador", resultado.perfil());
        assertEquals("Sem dados históricos. Perfil padrão.", resultado.descricao());
    }

    // --- Testes para recomendarProdutos ---

    @Test
    @DisplayName("Deve buscar produtos com Risco 'Baixo' para perfil 'Conservador'")
    void recomendarProdutos_DeveBuscarRiscoBaixo_ParaPerfilConservador() {
        // Cenário (Arrange) - (Regra de Negócio)
        String perfil = "Conservador";
        List<String> riscosEsperados = List.of("Baixo");

        // Simula o repositório retornando um produto de Risco Baixo
        List<ProdutoDTO> mockProdutos = List.of(
                new ProdutoDTO(1L, "CDB", "CDB", BigDecimal.ZERO, "Baixo")
        );
        when(produtoRepository.findRecomendadosPorRisco(riscosEsperados)).thenReturn(mockProdutos);

        // Ação (Act)
        List<ProdutoDTO> resultado = motorRecomendacaoService.recomendarProdutos(perfil);

        // Verificação (Assert)
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        // Verifica se o método do repositório foi chamado com a lista de riscos CORRETA
        verify(produtoRepository, times(1)).findRecomendadosPorRisco(riscosEsperados);
    }

    @Test
    @DisplayName("Deve buscar 'Baixo', 'Moderado' e 'Alto' para perfil 'Agressivo'")
    void recomendarProdutos_DeveBuscarTodosRiscos_ParaPerfilAgressivo() {
        // Cenário (Arrange) - (Regra de Negócio)
        String perfil = "Agressivo"; // Testando o Enum
        List<String> riscosEsperados = List.of("Baixo", "Moderado", "Alto");

        List<ProdutoDTO> mockProdutos = List.of(
                new ProdutoDTO(1L, "CDB", "CDB", BigDecimal.ZERO, "Baixo"),
                new ProdutoDTO(2L, "Fundo Mod", "Fundo", BigDecimal.ZERO, "Moderado"),
                new ProdutoDTO(3L, "Fundo Agr", "Fundo", BigDecimal.ZERO, "Alto")
        );
        when(produtoRepository.findRecomendadosPorRisco(riscosEsperados)).thenReturn(mockProdutos);

        // Ação (Act)
        List<ProdutoDTO> resultado = motorRecomendacaoService.recomendarProdutos(perfil);

        // Verificação (Assert)
        assertEquals(3, resultado.size());
        verify(produtoRepository, times(1)).findRecomendadosPorRisco(riscosEsperados);
    }

    @Test
    @DisplayName("Deve retornar lista vazia se o repositório não encontrar produtos")
    void recomendarProdutos_DeveRetornarListaVazia_QuandoRepositorioNaoAchar() {
        // Cenário (Arrange)
        String perfil = "Moderado";
        List<String> riscosEsperados = List.of("Baixo", "Moderado");

        // Simula o banco retornando uma lista vazia
        when(produtoRepository.findRecomendadosPorRisco(riscosEsperados)).thenReturn(Collections.emptyList());

        // Ação (Act)
        List<ProdutoDTO> resultado = motorRecomendacaoService.recomendarProdutos(perfil);

        // Verificação (Assert)
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(produtoRepository, times(1)).findRecomendadosPorRisco(riscosEsperados);
    }

    @Test
    @DisplayName("Deve lançar ValidacaoNegocioException para perfil de risco inválido")
    void recomendarProdutos_DeveLancarExcecao_ParaPerfilInvalido() {
        // Cenário (Arrange) - (Falha: Dados Incorretos)
        String perfilInvalido = "Iniciante";

        // Ação & Verificação (Act & Assert)
        var excecao = assertThrows(ValidacaoNegocioException.class, () -> {
            motorRecomendacaoService.recomendarProdutos(perfilInvalido);
        });

        assertTrue(excecao.getMessage().contains("Perfil de risco inválido: 'Iniciante'"));

        // Garante que NUNCA chamamos o banco de dados se o perfil for inválido
        verify(produtoRepository, never()).findRecomendadosPorRisco(any());
    }
}
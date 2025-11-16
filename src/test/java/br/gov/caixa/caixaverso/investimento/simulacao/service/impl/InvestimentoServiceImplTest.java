package br.gov.caixa.caixaverso.investimento.simulacao.service.impl;

import br.gov.caixa.caixaverso.investimento.exception.ValidacaoNegocioException;
import br.gov.caixa.caixaverso.investimento.recomendacao.dto.ProdutoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.data.SimulacaoRepository;
import br.gov.caixa.caixaverso.investimento.simulacao.domain.Produto;
import br.gov.caixa.caixaverso.investimento.simulacao.domain.Simulacao;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.HistoricoSimulacaoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoRequestDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoResponseDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.mapper.ProdutoMapper;
import br.gov.caixa.caixaverso.investimento.simulacao.service.ProdutoValidationService;
import br.gov.caixa.caixaverso.investimento.simulacao.service.SimulacaoPersistenceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste Unitário - InvestimentoService (Orquestrador)")
class InvestimentoServiceImplTest {

    // --- Mocks para todas as dependências ---
    @Mock
    private SimulacaoRepository simulacaoRepository;
    @Mock
    private ProdutoMapper produtoMapper;
    @Mock
    private ProdutoValidationService produtoValidationService;
    @Mock
    private SimulacaoPersistenceService simulacaoPersistenceService;

    // --- Classe sob teste ---
    @InjectMocks
    private InvestimentoServiceImpl investimentoService;

    // --- Testes para simularEValidar ---

    @Test
    @DisplayName("Deve orquestrar a simulação com sucesso no caminho feliz")
    void simularEValidar_DeveOrquestrarComSucesso() {
        // Cenário (Arrange)
        // 1. Dados de entrada
        SimulacaoRequestDTO request = new SimulacaoRequestDTO(1L, new BigDecimal("1000"), 12, "CDB");

        // 2. Mockar o Serviço de Validação (Passo 1 do método)
        Produto mockProduto = mock(Produto.class);
        when(mockProduto.getRentabilidadeAnual()).thenReturn(new BigDecimal("0.10")); // Necessário para o cálculo
        when(produtoValidationService.validarProduto(request)).thenReturn(mockProduto);

        // 3. Mockar o Mapper (Passo 5 do método)
        ProdutoDTO mockProdutoDTO = mock(ProdutoDTO.class);
        when(produtoMapper.toDTO(mockProduto)).thenReturn(mockProdutoDTO);

        // 4. Preparar o ArgumentCaptor para o serviço de persistência (Passo 4)
        ArgumentCaptor<Simulacao> simulacaoCaptor = ArgumentCaptor.forClass(Simulacao.class);

        // Ação (Act)
        SimulacaoResponseDTO response = investimentoService.simularEValidar(request);

        // Verificação (Assert)
        // Verifica a orquestração (se os serviços corretos foram chamados)
        verify(produtoValidationService, times(1)).validarProduto(request);
        verify(produtoMapper, times(1)).toDTO(mockProduto);

        // Verifica se a persistência assíncrona foi chamada
        // Usamos o captor para verificar o objeto que foi passado para o método async
        verify(simulacaoPersistenceService, times(1)).persistirSimulacaoAsync(simulacaoCaptor.capture());

        // Verifica os dados que foram para o banco
        Simulacao simulacaoSalva = simulacaoCaptor.getValue();
        assertEquals(1L, simulacaoSalva.getClienteId());
        assertEquals(new BigDecimal("1000"), simulacaoSalva.getValorInvestido());
        // Verifica se o cálculo (privado) foi correto
        assertEquals(0, new BigDecimal("1100.00").compareTo(simulacaoSalva.getValorFinal()));

        // Verifica a resposta final
        assertNotNull(response);
        assertEquals(mockProdutoDTO, response.produtoValidado());
        assertEquals(0, new BigDecimal("1100.00").compareTo(response.resultadoSimulacao().valorFinal()));
    }

    @Test
    @DisplayName("Deve lançar exceção e não persistir se a validação do produto falhar")
    void simularEValidar_DeveLancarExcecao_QuandoValidacaoFalhar() {
        // Cenário (Arrange) - (Falha: Regra de Negócio)
        SimulacaoRequestDTO request = new SimulacaoRequestDTO(1L, new BigDecimal("100"), 1, "Acoes");

        // Simula o serviço de validação lançando uma exceção
        when(produtoValidationService.validarProduto(request))
                .thenThrow(new ValidacaoNegocioException("Produto 'Acoes' não existe"));

        // Ação & Verificação (Act & Assert)
        var excecao = assertThrows(ValidacaoNegocioException.class, () -> {
            investimentoService.simularEValidar(request);
        });

        assertEquals("Produto 'Acoes' não existe", excecao.getMessage());

        // Verifica se as etapas seguintes NUNCA foram chamadas
        verify(simulacaoPersistenceService, never()).persistirSimulacaoAsync(any());
        verify(produtoMapper, never()).toDTO(any());
    }

    // --- Testes para os métodos GET (Pass-through) ---

    @Test
    @DisplayName("Deve chamar o repositório para buscarHistorico")
    void buscarHistorico_DeveChamarRepositorio() {
        // Cenário (Arrange)
        List<HistoricoSimulacaoDTO> mockLista = Collections.emptyList();
        when(simulacaoRepository.findHistoricoCompleto()).thenReturn(mockLista);

        // Ação (Act)
        List<HistoricoSimulacaoDTO> resultado = investimentoService.buscarHistorico();

        // Verificação (Assert)
        assertNotNull(resultado);
        assertEquals(mockLista, resultado);
        verify(simulacaoRepository, times(1)).findHistoricoCompleto();
    }

    @Test
    @DisplayName("Deve chamar o repositório para buscarSimulacoesAgregadas")
    void buscarSimulacoesAgregadas_DeveChamarRepositorio() {
        // Cenário (Arrange)
        when(simulacaoRepository.findSimulacoesAgregadasPorProdutoEDia()).thenReturn(Collections.emptyList());

        // Ação (Act)
        investimentoService.buscarSimulacoesAgregadas();

        // Verificação (Assert)
        verify(simulacaoRepository, times(1)).findSimulacoesAgregadasPorProdutoEDia();
    }

    @Test
    @DisplayName("Deve chamar o repositório para buscarHistoricoInvestimentos com o ID correto")
    void buscarHistoricoInvestimentos_DeveChamarRepositorioComId() {
        // Cenário (Arrange)
        Long clienteId = 123L;
        when(simulacaoRepository.findHistoricoInvestimentosPorClienteId(clienteId)).thenReturn(Collections.emptyList());

        // Ação (Act)
        investimentoService.buscarHistoricoInvestimentos(clienteId);

        // Verificação (Assert)
        verify(simulacaoRepository, times(1)).findHistoricoInvestimentosPorClienteId(clienteId);
    }

    @Test
    @DisplayName("Deve propagar exceções do banco de dados nos métodos GET")
    void buscarHistorico_DevePropagarExcecao_QuandoRepositorioFalhar() {
        // Cenário (Arrange) - (Falha: Infraestrutura)
        when(simulacaoRepository.findHistoricoCompleto())
                .thenThrow(new DataAccessException("Erro de banco de dados") {}); // Exceção genérica do Spring Data

        // Ação & Verificação (Act & Assert)
        assertThrows(DataAccessException.class, () -> {
            investimentoService.buscarHistorico();
        });
    }
}
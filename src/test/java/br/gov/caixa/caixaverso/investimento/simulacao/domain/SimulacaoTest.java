package br.gov.caixa.caixaverso.investimento.simulacao.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class) // Habilita o uso de @Mock
@DisplayName("Teste Unitário - Domínio Simulacao")
class SimulacaoTest {

    // Como 'Produto' é outra entidade, nós a mockamos.
    // Não queremos testar o 'Produto' aqui.
    @Mock
    private Produto mockProduto;

    private Long clienteId;
    private BigDecimal valorInvestido;
    private Integer prazoMeses;
    private BigDecimal valorFinal;

    @BeforeEach
    void setUp() {
        // Prepara dados de cenário comuns
        clienteId = 123L;
        valorInvestido = new BigDecimal("10000.00");
        prazoMeses = 12;
        valorFinal = new BigDecimal("11200.00");
    }

    // --- Testes de Sucesso (Regras de Negócio) ---

    @Test
    @DisplayName("Deve construir a entidade Simulacao corretamente com dados válidos")
    void construtor_DeveAtribuirCampos_QuandoDadosSaoValidos() {
        // Cenário
        Instant agora = Instant.now();

        // Ação
        Simulacao simulacao = new Simulacao(
                clienteId,
                mockProduto,
                valorInvestido,
                prazoMeses,
                valorFinal
        );

        // Verificação
        assertNotNull(simulacao);
        assertEquals(clienteId, simulacao.getClienteId());
        assertEquals(mockProduto, simulacao.getProduto());
        assertEquals(valorInvestido, simulacao.getValorInvestido());
        assertEquals(prazoMeses, simulacao.getPrazoMeses());
        assertEquals(valorFinal, simulacao.getValorFinal());

        // Verifica se a data foi definida automaticamente
        assertNotNull(simulacao.getDataSimulacao());
        // Compara os segundos, ignorando nanossegundos (devido ao truncatedTo)
        assertEquals(
                agora.truncatedTo(ChronoUnit.SECONDS),
                simulacao.getDataSimulacao().truncatedTo(ChronoUnit.SECONDS)
        );
    }

    // --- Testes de Falha (Dados Incorretos / Regras de Negócio) ---

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando clienteId for nulo")
    void construtor_DeveLancarExcecao_QuandoClienteIdForNulo() {
        // Cenário (Falha: Dados Incorretos)
        Long clienteIdNulo = null;

        // Ação & Verificação
        var excecao = assertThrows(IllegalArgumentException.class, () -> {
            new Simulacao(
                    clienteIdNulo, // Dado inválido
                    mockProduto,
                    valorInvestido,
                    prazoMeses,
                    valorFinal
            );
        });

        // Verifica a mensagem de erro (boa prática)
        assertEquals("ClienteId não pode ser nulo", excecao.getMessage());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando Produto for nulo")
    void construtor_DeveLancarExcecao_QuandoProdutoForNulo() {
        // Cenário (Falha: Dados Incorretos)
        Produto produtoNulo = null;

        // Ação & Verificação
        var excecao = assertThrows(IllegalArgumentException.class, () -> {
            new Simulacao(
                    clienteId,
                    produtoNulo, // Dado inválido
                    valorInvestido,
                    prazoMeses,
                    valorFinal
            );
        });

        // Verifica a mensagem de erro
        assertEquals("Produto não pode ser nulo", excecao.getMessage());
    }
}
package br.gov.caixa.caixaverso.investimento.simulacao.data;

import br.gov.caixa.caixaverso.investimento.recomendacao.dto.PerfilAgregadoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.domain.Produto;
import br.gov.caixa.caixaverso.investimento.simulacao.domain.Simulacao;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.HistoricoSimulacaoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.InvestimentoHistoricoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoAgregadaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Teste de Integração - SimulacaoRepository")
class SimulacaoRepositoryIT {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SimulacaoRepository simulacaoRepository;

    @Autowired
    private ProdutoRepository produtoRepository; // Usado para pegar os produtos do data.sql

    // Produtos carregados do data.sql
    private Produto cdb2026, fundoXpto, fundoModerado, lca2025;

    // Datas de teste
    private final Instant HOJE = Instant.now();
    private final Instant ONTEM = HOJE.minus(1, ChronoUnit.DAYS);

    @BeforeEach
    void setUp() {
        // Carrega os produtos do data.sql para linkar nas simulações
        cdb2026 = produtoRepository.findAll().stream().filter(p -> p.getNome().equals("CDB Caixa 2026")).findFirst().get();
        fundoXpto = produtoRepository.findAll().stream().filter(p -> p.getNome().equals("Fundo XPTO")).findFirst().get();
        fundoModerado = produtoRepository.findAll().stream().filter(p -> p.getNome().equals("Fundo Caixa Moderado")).findFirst().get();
        lca2025 = produtoRepository.findAll().stream().filter(p -> p.getNome().equals("LCA Caixa Agronegócio 2025")).findFirst().get();
    }

    /**
     * Helper para criar, setar a data e persistir uma simulação
     */
    private Simulacao persistSimulacao(Long clienteId, Produto p, BigDecimal vInvest, int prazo, BigDecimal vFinal, Instant data) {
        Simulacao s = new Simulacao(clienteId, p, vInvest, prazo, vFinal);
        s.setDataSimulacao(data); // Seta a data para o teste
        return entityManager.persistAndFlush(s);
    }

    // --- Testes de Sucesso (Casos do PDF) ---
    @Test
    @DisplayName("Sucesso (PDF): Deve salvar e recuperar simulação do CDB Caixa 2026")
    void save_CasoPDF_CDB_DevePersistirCorretamente() {
        // Cenário (Arrange)
        Simulacao s = persistSimulacao(123L, cdb2026, new BigDecimal("10000.00"), 12, new BigDecimal("11200.00"), HOJE);

        // Ação (Act)
        Simulacao encontrada = simulacaoRepository.findById(s.getId()).orElse(null);

        // Verificação (Assert)
        assertThat(encontrada).isNotNull();
        assertThat(encontrada.getClienteId()).isEqualTo(123L);
        assertThat(encontrada.getProduto().getNome()).isEqualTo("CDB Caixa 2026");
        assertThat(encontrada.getValorInvestido()).isEqualByComparingTo("10000.00");
    }

    @Test
    @DisplayName("Sucesso (PDF): Deve salvar e recuperar simulação do Fundo XPTO")
    void save_CasoPDF_Fundo_DevePersistirCorretamente() {
        // Cenário (Arrange)
        Simulacao s = persistSimulacao(123L, fundoXpto, new BigDecimal("5000.00"), 6, new BigDecimal("5800.00"), HOJE);

        // Ação (Act)
        Simulacao encontrada = simulacaoRepository.findById(s.getId()).orElse(null);

        // Verificação (Assert)
        assertThat(encontrada).isNotNull();
        assertThat(encontrada.getProduto().getNome()).isEqualTo("Fundo XPTO");
        assertThat(encontrada.getValorInvestido()).isEqualByComparingTo("5000.00");
    }

    // --- Testes para findSimulacoesAgregadasPorProdutoEDia ---
    @Nested
    @DisplayName("Query: findSimulacoesAgregadasPorProdutoEDia")
    class FindAgregadasTest {

        @Test
        @DisplayName("Sucesso (Regra): Deve agrupar por produto e calcular COUNT e AVG")
        void findAgregadas_DeveAgruparPorProduto_E_CalcularMetricas() {
            // Cenário (Arrange): 2 CDBs (cliente 1), 1 Fundo (cliente 2)
            persistSimulacao(1L, cdb2026, new BigDecimal("1000"), 12, new BigDecimal("1120"), HOJE);
            persistSimulacao(1L, cdb2026, new BigDecimal("2000"), 12, new BigDecimal("2240"), HOJE);
            persistSimulacao(2L, fundoXpto, new BigDecimal("5000"), 6, new BigDecimal("5800"), HOJE);

            // Ação (Act)
            List<SimulacaoAgregadaDTO> resultado = simulacaoRepository.findSimulacoesAgregadasPorProdutoEDia();

            // Verificação (Assert)
            assertThat(resultado).hasSize(2); // 2 Grupos (CDB e Fundo)

            SimulacaoAgregadaDTO cdbGrupo = resultado.stream().filter(r -> r.produto().equals("CDB Caixa 2026")).findFirst().get();
            assertThat(cdbGrupo.quantidadeSimulacoes()).isEqualTo(2);
            // Média de 1120 e 2240 = 1680
            assertThat(cdbGrupo.mediaValorFinal()).isEqualByComparingTo("1680.00");
        }

        @Test
        @DisplayName("Sucesso (Regra): Deve agrupar por Dia (Partição de Equivalência)")
        void findAgregadas_DeveAgruparPorDia() {
            // Cenário (Arrange): 1 CDB hoje, 1 CDB ontem
            persistSimulacao(1L, cdb2026, new BigDecimal("1000"), 12, new BigDecimal("1120"), HOJE);
            persistSimulacao(1L, cdb2026, new BigDecimal("1000"), 12, new BigDecimal("1120"), ONTEM);

            // Ação (Act)
            List<SimulacaoAgregadaDTO> resultado = simulacaoRepository.findSimulacoesAgregadasPorProdutoEDia();

            // Verificação (Assert)
            assertThat(resultado).hasSize(2); // 2 Grupos (CDB-Hoje, CDB-Ontem)
        }

        @Test
        @DisplayName("Falha (Limite): Deve retornar lista vazia se não houver simulações")
        void findAgregadas_SemSimulacoes_DeveRetornarListaVazia() {
            // Ação (Act)
            List<SimulacaoAgregadaDTO> resultado = simulacaoRepository.findSimulacoesAgregadasPorProdutoEDia();
            // Verificação (Assert)
            assertThat(resultado).isEmpty();
        }
    }

    // --- Testes para findHistoricoCompleto ---
    @Nested
    @DisplayName("Query: findHistoricoCompleto (Projeção)")
    class FindHistoricoCompletoTest {

        @Test
        @DisplayName("Sucesso: Deve retornar todas as simulações como DTOs")
        void findHistoricoCompleto_DeveRetornarTodosOsDTOs() {
            // Cenário (Arrange)
            persistSimulacao(1L, cdb2026, new BigDecimal("1000"), 12, new BigDecimal("1120"), HOJE);
            persistSimulacao(2L, fundoXpto, new BigDecimal("5000"), 6, new BigDecimal("5800"), ONTEM);

            // Ação (Act)
            List<HistoricoSimulacaoDTO> resultado = simulacaoRepository.findHistoricoCompleto();

            // Verificação (Assert)
            assertThat(resultado).hasSize(2);
        }

        @Test
        @DisplayName("Sucesso (Projeção): Deve mapear campos do DTO corretamente")
        void findHistoricoCompleto_DeveMapearDTOcorretamente() {
            // Cenário (Arrange)
            persistSimulacao(123L, cdb2026, new BigDecimal("1000"), 12, new BigDecimal("1120"), HOJE);

            // Ação (Act)
            HistoricoSimulacaoDTO dto = simulacaoRepository.findHistoricoCompleto().get(0);

            // Verificação (Assert)
            assertThat(dto.clienteId()).isEqualTo(123L);
            assertThat(dto.produto()).isEqualTo("CDB Caixa 2026");
            assertThat(dto.valorInvestido()).isEqualByComparingTo("1000");
            assertThat(dto.valorFinal()).isEqualByComparingTo("1120");
        }

        @Test
        @DisplayName("Falha (Limite): Deve retornar lista vazia se não houver simulações")
        void findHistoricoCompleto_SemSimulacoes_DeveRetornarListaVazia() {
            // Ação (Act)
            List<HistoricoSimulacaoDTO> resultado = simulacaoRepository.findHistoricoCompleto();
            // Verificação (Assert)
            assertThat(resultado).isEmpty();
        }
    }

    // --- Testes para findHistoricoInvestimentosPorClienteId ---
    @Nested
    @DisplayName("Query: findHistoricoInvestimentosPorClienteId (Projeção + Partição)")
    class FindHistoricoPorClienteTest {

        @Test
        @DisplayName("Sucesso (Partição): Deve retornar apenas simulações do Cliente 1")
        void findHistoricoPorCliente_DeveFiltrarPorClienteId() {
            // Cenário (Arrange): Cliente 1 (2 sims), Cliente 2 (1 sim)
            persistSimulacao(1L, cdb2026, new BigDecimal("1000"), 12, new BigDecimal("1120"), HOJE);
            persistSimulacao(1L, lca2025, new BigDecimal("2000"), 12, new BigDecimal("2200"), HOJE);
            persistSimulacao(2L, fundoXpto, new BigDecimal("5000"), 6, new BigDecimal("5800"), ONTEM);

            // Ação (Act)
            List<InvestimentoHistoricoDTO> resultado = simulacaoRepository.findHistoricoInvestimentosPorClienteId(1L);

            // Verificação (Assert)
            assertThat(resultado).hasSize(2);
        }

        @Test
        @DisplayName("Sucesso (Projeção): Deve mapear campos do DTO corretamente")
        void findHistoricoPorCliente_DeveMapearDTOcorretamente() {
            // Cenário (Arrange)
            persistSimulacao(123L, cdb2026, new BigDecimal("1000"), 12, new BigDecimal("1120"), HOJE);

            // Ação (Act)
            InvestimentoHistoricoDTO dto = simulacaoRepository.findHistoricoInvestimentosPorClienteId(123L).get(0);

            LocalDate dataEsperada = LocalDate.ofInstant(HOJE, ZoneId.systemDefault());

            // Verificação (Assert)
            assertThat(dto.tipo()).isEqualTo("CDB");
            assertThat(dto.valor()).isEqualByComparingTo("1000");
            assertThat(dto.rentabilidade()).isEqualByComparingTo("0.12");
            assertThat(dto.data()).isEqualTo(dataEsperada);
        }

        @Test
        @DisplayName("Falha (Partição): Deve retornar lista vazia para cliente sem simulações")
        void findHistoricoPorCliente_ClienteSemSimulacoes_DeveRetornarListaVazia() {
            // Cenário (Arrange)
            persistSimulacao(1L, cdb2026, new BigDecimal("1000"), 12, new BigDecimal("1120"), HOJE);

            // Ação (Act)
            List<InvestimentoHistoricoDTO> resultado = simulacaoRepository.findHistoricoInvestimentosPorClienteId(2L);

            // Verificação (Assert)
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Falha (Limite): Deve retornar lista vazia se não houver simulações")
        void findHistoricoPorCliente_SemSimulacoes_DeveRetornarListaVazia() {
            // Ação (Act)
            List<InvestimentoHistoricoDTO> resultado = simulacaoRepository.findHistoricoInvestimentosPorClienteId(1L);
            // Verificação (Assert)
            assertThat(resultado).isEmpty();
        }
    }

    // --- Testes para findPerfilAgregadoPorCliente ---
    @Nested
    @DisplayName("Query: findPerfilAgregadoPorCliente (Agregação)")
    class FindPerfilAgregadoTest {

        @Test
        @DisplayName("Sucesso (Regra): Deve calcular AVG de Valor e Risco Baixo")
        void findPerfilAgregado_DeveCalcularAvgCorretamente_RiscoBaixo() {
            // Cenário (Arrange): 2 sims "Baixo" (10 pts)
            persistSimulacao(1L, cdb2026, new BigDecimal("1000"), 12, new BigDecimal("1120"), HOJE);
            persistSimulacao(1L, lca2025, new BigDecimal("3000"), 12, new BigDecimal("3300"), HOJE);

            // Ação (Act)
            PerfilAgregadoDTO resultado = simulacaoRepository.findPerfilAgregadoPorCliente(1L);

            // Verificação (Assert)
            assertThat(resultado.contagem()).isEqualTo(2);
            // Média de 1000 e 3000 = 2000
            assertThat(resultado.mediaValor()).isEqualByComparingTo(2000.0);
            // Média de Risco (10 e 10) = 10
            assertThat(resultado.mediaRisco()).isEqualByComparingTo(10.0);
        }

        @Test
        @DisplayName("Sucesso (Regra): Deve calcular AVG de Risco Misto (Baixo, Mod, Alto)")
        void findPerfilAgregado_DeveCalcularAvgCorretamente_RiscoMisto() {
            // Cenário (Arrange): 1 Baixo (10), 1 Moderado (20), 1 Alto (34)
            persistSimulacao(1L, cdb2026, new BigDecimal("1000"), 12, new BigDecimal("1120"), HOJE);
            persistSimulacao(1L, fundoModerado, new BigDecimal("2000"), 24, new BigDecimal("2300"), HOJE);
            persistSimulacao(1L, fundoXpto, new BigDecimal("5000"), 6, new BigDecimal("5800"), HOJE);

            // Ação (Act)
            PerfilAgregadoDTO resultado = simulacaoRepository.findPerfilAgregadoPorCliente(1L);

            // Verificação (Assert)
            assertThat(resultado.contagem()).isEqualTo(3);
            // Média de Risco (10 + 20 + 34) / 3 = 64 / 3 = 21.333...
            assertThat(resultado.mediaRisco()).isBetween(21.33, 21.34);
        }

        @Test
        @DisplayName("Falha (Limite): Deve retornar Contagem 0 e Médias Nulas para cliente novo")
        void findPerfilAgregado_ClienteNovo_DeveRetornarContagemZeroEMediasNulas() {
            // Ação (Act)
            PerfilAgregadoDTO resultado = simulacaoRepository.findPerfilAgregadoPorCliente(999L);

            // Verificação (Assert)
            assertThat(resultado).isNotNull();
            assertThat(resultado.contagem()).isZero();
            assertThat(resultado.mediaValor()).isNull();
            assertThat(resultado.mediaRisco()).isNull();
        }
    }
}
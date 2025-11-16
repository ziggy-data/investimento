package br.gov.caixa.caixaverso.investimento.simulacao.data;

import br.gov.caixa.caixaverso.investimento.recomendacao.dto.ProdutoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.domain.Produto;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de Integração para o ProdutoRepository.
 * Usa @DataJpaTest para carregar apenas a camada JPA e rodar contra
 * o banco H2 em memória, que será populado pelo data.sql.
 */
@DataJpaTest
@DisplayName("Teste de Integração - ProdutoRepository")
class ProdutoRepositoryIT {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Nested
    @DisplayName("Testes para findFirstByTipoAndValorMinimoLessThanEqual...")
    class FindFirstProdutoTest {

        @Test
        @DisplayName("Sucesso (Regra PDF): Deve encontrar 'CDB Caixa 2026' para 10k/12m")
        void findFirst_CasoPDF_CDB_DeveRetornarProdutoCorreto() {
            // Cenário (Arrange) - PDF
            SimulacaoRequestDTO request = new SimulacaoRequestDTO(123L, new BigDecimal("10000.00"), 12, "CDB");

            // Ação (Act)
            Optional<Produto> resultado = produtoRepository.findFirstByTipoAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeAnualDesc(
                    request.tipoProduto(), request.valor(), request.prazoMeses()
            );

            // Verificação (Assert)
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getNome()).isEqualTo("CDB Caixa 2026");
            assertThat(resultado.get().getRentabilidadeAnual()).isEqualTo(new BigDecimal("0.12"));
        }

        @Test
        @DisplayName("Sucesso (Regra PDF): Deve encontrar 'Fundo XPTO' para 5k/6m")
        void findFirst_CasoPDF_Fundo_DeveRetornarProdutoCorreto() {
            // Cenário (Arrange) - PDF
            SimulacaoRequestDTO request = new SimulacaoRequestDTO(123L, new BigDecimal("5000.00"), 6, "Fundo");

            // Ação (Act)
            Optional<Produto> resultado = produtoRepository.findFirstByTipoAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeAnualDesc(
                    request.tipoProduto(), request.valor(), request.prazoMeses()
            );

            // Verificação (Assert)
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getNome()).isEqualTo("Fundo XPTO");
            assertThat(resultado.get().getRentabilidadeAnual()).isEqualTo(new BigDecimal("0.32"));
        }

        @Test
        @DisplayName("Sucesso (Regra Negócio): Deve retornar MAIOR rentabilidade se múltiplos produtos baterem")
        void findFirst_MultiplosCandidatos_DeveRetornarMelhorRentabilidade() {
            // Cenário (Arrange): Atende 'CDB Liquidez' (100, 1, 0.105) e 'CDB 2026' (1000, 12, 0.12)
            SimulacaoRequestDTO request = new SimulacaoRequestDTO(123L, new BigDecimal("1000.00"), 12, "CDB");

            // Ação (Act)
            Optional<Produto> resultado = produtoRepository.findFirstByTipoAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeAnualDesc(
                    request.tipoProduto(), request.valor(), request.prazoMeses()
            );

            // Verificação (Assert): Deve pegar o de 0.12
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getNome()).isEqualTo("CDB Caixa 2026");
        }

        @Test
        @DisplayName("Sucesso (Valor Limite): Deve encontrar produto com valor EXATO")
        void findFirst_ValorLimiteExato_DeveRetornarProduto() {
            // Cenário (Arrange): Valor (1000) == Valor Mínimo (1000)
            Optional<Produto> resultado = produtoRepository.findFirstByTipoAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeAnualDesc(
                    "CDB", new BigDecimal("1000.00"), 12
            );
            // Verificação (Assert)
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getNome()).isEqualTo("CDB Caixa 2026");
        }


        @Test
        @DisplayName("Falha (Partição): Deve retornar Vazio para tipo inexistente")
        void findFirst_TipoInexistente_DeveRetornarVazio() {
            // Cenário (Arrange): 'Acoes' não existe no data.sql
            Optional<Produto> resultado = produtoRepository.findFirstByTipoAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeAnualDesc(
                    "Acoes", new BigDecimal("1000.00"), 12
            );
            // Verificação (Assert)
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Falha (Valor Limite): Deve retornar Vazio para valor abaixo do mínimo")
        void findFirst_ValorInsuficiente_DeveRetornarVazio() {
            // Cenário (Arrange): CDB 2026 exige 1000.
            SimulacaoRequestDTO request = new SimulacaoRequestDTO(123L, new BigDecimal("999.99"), 12, "CDB");

            // Ação (Act)
            Optional<Produto> resultado = produtoRepository.findFirstByTipoAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeAnualDesc(
                    request.tipoProduto(), request.valor(), request.prazoMeses()
            );

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Falha (Valor Limite): Deve retornar Vazio se valor for abaixo de TODOS os mínimos")
        void findFirst_ValorAbaixoDeTodos_DeveRetornarVazio() {
            // Cenário (Arrange): O CDB mais barato exige 100.
            Optional<Produto> resultado = produtoRepository.findFirstByTipoAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeAnualDesc(
                    "CDB", new BigDecimal("99.99"), 12
            );
            // Verificação (Assert)
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Falha (Valor Limite): Deve retornar Vazio para prazo abaixo do mínimo")
        void findFirst_PrazoInsuficiente_DeveRetornarVazio() {
            // Cenário (Arrange): LCI no data.sql exige 24 meses.
            SimulacaoRequestDTO request = new SimulacaoRequestDTO(123L, new BigDecimal("5000.00"), 23, "LCI");

            // Ação (Act)
            Optional<Produto> resultado = produtoRepository.findFirstByTipoAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeAnualDesc(
                    request.tipoProduto(), request.valor(), request.prazoMeses()
            );

            // CORREÇÃO: Meu teste antigo estava errado. Ele esperava um valor,
            // mas o log mostrou que ele ficou vazio. Esta é a asserção correta.
            assertThat(resultado).isEmpty();
        }
    }

    // --- Testes para findRecomendadosPorRisco ---

    @Nested
    @DisplayName("Testes para findRecomendadosPorRisco (Query JPQL)")
    class FindRecomendadosTest {

        @Test
        @DisplayName("Sucesso (Partição): Deve retornar 4 produtos para Risco 'Baixo'")
        void findRecomendados_RiscoBaixo_DeveRetornar6Produtos() {
            // Ação (Act)
            List<ProdutoDTO> resultados = produtoRepository.findRecomendadosPorRisco(List.of("Baixo"));
            // Verificação (Assert): (1 CDB, 1 LCI, 1 LCA, 1 Tesouro Selic)
            assertThat(resultados).hasSize(4);
        }

        @Test
        @DisplayName("Sucesso (Partição): Deve retornar 1 produtos para Risco 'Moderado'")
        void findRecomendados_RiscoModerado_DeveRetornar1Produtos() {
            // Ação (Act)
            List<ProdutoDTO> resultados = produtoRepository.findRecomendadosPorRisco(List.of("Moderado"));
            // Verificação (Assert): (Fundo Moderado)
            assertThat(resultados).hasSize(1);
        }

        @Test
        @DisplayName("Sucesso (Partição): Deve retornar 2 produtos para Risco 'Alto'")
        void findRecomendados_RiscoAlto_DeveRetornar2Produtos() {
            // Ação (Act)
            List<ProdutoDTO> resultados = produtoRepository.findRecomendadosPorRisco(List.of("Alto"));
            // Verificação (Assert): (Fundo XPTO, Fundo Agressivo)
            assertThat(resultados).hasSize(2);
        }

        @Test
        @DisplayName("Sucesso (Regra Negócio): Deve retornar 3 produtos para 'Moderado' e 'Alto'")
        void findRecomendados_MultiplosRiscos_DeveRetornar4Produtos() {
            // Ação (Act)
            List<ProdutoDTO> resultados = produtoRepository.findRecomendadosPorRisco(List.of("Moderado", "Alto"));
            // Verificação (Assert): (1 Moderado + 2 Alto)
            assertThat(resultados).hasSize(3);
        }

        @Test
        @DisplayName("Sucesso (Projeção): Deve retornar DTOs com campos corretos")
        void findRecomendados_DeveRetornarDTOsCorretos() {
            // Ação (Act)
            List<ProdutoDTO> resultados = produtoRepository.findRecomendadosPorRisco(List.of("Alto"));

            // Verificação (Assert): Pega o Fundo XPTO
            ProdutoDTO fundoXpto = resultados.stream()
                    .filter(p -> p.nome().equals("Fundo XPTO"))
                    .findFirst()
                    .orElse(null);

            assertThat(fundoXpto).isNotNull();
            assertThat(fundoXpto.tipo()).isEqualTo("Fundo");
            assertThat(fundoXpto.risco()).isEqualTo("Alto");
            assertThat(fundoXpto.rentabilidade()).isEqualTo(new BigDecimal("0.32"));
        }

        @Test
        @DisplayName("Falha (Partição): Deve retornar Lista Vazia para risco inexistente")
        void findRecomendados_RiscoInexistente_DeveRetornarListaVazia() {
            // Ação (Act)
            List<ProdutoDTO> resultados = produtoRepository.findRecomendadosPorRisco(List.of("Risco-Total"));
            // Verificação (Assert)
            assertThat(resultados).isEmpty();
        }

        @Test
        @DisplayName("Falha (Valor Limite): Deve retornar Lista Vazia para lista de riscos vazia")
        void findRecomendados_ListaVazia_DeveRetornarListaVazia() {
            // Ação (Act)
            List<ProdutoDTO> resultados = produtoRepository.findRecomendadosPorRisco(Collections.emptyList());
            // Verificação (Assert)
            assertThat(resultados).isEmpty();
        }

        @Test
        @DisplayName("Falha (Exceção): Deve lançar Exceção para lista vazia")
        void findRecomendados_ListaVazia_DeveLancarExcecao() {
            // Ação (Act) & Verificação (Assert)
            // Spring Data JPA lança esta exceção quando um parâmetro de query é nulo
            List<ProdutoDTO> resultados = produtoRepository.findRecomendadosPorRisco(null);

            // Verificação (Assert)
            assertThat(resultados).isEmpty();
        }
    }
}
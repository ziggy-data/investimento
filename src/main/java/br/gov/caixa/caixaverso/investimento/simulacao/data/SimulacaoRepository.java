package br.gov.caixa.caixaverso.investimento.simulacao.data;

import br.gov.caixa.caixaverso.investimento.recomendacao.dto.PerfilAgregadoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.HistoricoSimulacaoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.InvestimentoHistoricoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoAgregadaDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.domain.Simulacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

// Repositório para a entidade Simulacao
public interface SimulacaoRepository extends JpaRepository<Simulacao, Long> {

    /**
     * Busca dados agregados de simulações, agrupados por produto e dia.
     * Esta consulta usa uma Projeção de Construtor JPQL para mapear o resultado
     * diretamente para o nosso SimulacaoAgregadaDTO.
     * <p>
     * CAST(s.dataSimulacao AS LocalDate) agrupa todos os timestamps
     * (Instant) dentro do mesmo dia.
     *
     * @return Lista de DTOs agregados.
     */
    @Query("SELECT new br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoAgregadaDTO(" +
            "   s.produto.nome, " +
            "   CAST(s.dataSimulacao AS LocalDate), " +
            "   COUNT(s.id), " +
            "   AVG(s.valorFinal)) " +
            "FROM Simulacao s " +
            "GROUP BY s.produto.nome, CAST(s.dataSimulacao AS LocalDate)")
    List<SimulacaoAgregadaDTO> findSimulacoesAgregadasPorProdutoEDia();


    /**
     * OTIMIZAÇÃO: Busca o histórico já no formato DTO.
     */
    @Query("SELECT new br.gov.caixa.caixaverso.investimento.simulacao.dto.HistoricoSimulacaoDTO(" +
            "   s.id, s.clienteId, s.produto.nome, s.valorInvestido, s.valorFinal, " +
            "   s.prazoMeses, s.dataSimulacao) " +
            "FROM Simulacao s")
    List<HistoricoSimulacaoDTO> findHistoricoCompleto();

    /**
     * OTIMIZAÇÃO: Busca o histórico do cliente já no formato DTO.
     */
    @Query("SELECT new br.gov.caixa.caixaverso.investimento.simulacao.dto.InvestimentoHistoricoDTO(" +
            "   s.id, s.produto.tipo, s.valorInvestido, s.produto.rentabilidadeAnual, " +
            "   CAST(s.dataSimulacao AS LocalDate)) " +
            "FROM Simulacao s WHERE s.clienteId = :clienteId")
    List<InvestimentoHistoricoDTO> findHistoricoInvestimentosPorClienteId(Long clienteId);

    /**
     * Executa o cálculo do perfil de risco (COUNT, AVG de valor,
     * e AVG de risco) diretamente no banco de dados, retornando um único DTO.
     */
    @Query("SELECT new br.gov.caixa.caixaverso.investimento.recomendacao.dto.PerfilAgregadoDTO(" +
            "   COUNT(s.id), " +
            "   AVG(s.valorInvestido), " +
            "   AVG(CASE s.produto.risco WHEN 'Alto' THEN 34.0 WHEN 'Moderado' THEN 20.0 ELSE 10.0 END)" +
            ") " +
            "FROM Simulacao s " +
            "WHERE s.clienteId = :clienteId")
    PerfilAgregadoDTO findPerfilAgregadoPorCliente(Long clienteId);
}

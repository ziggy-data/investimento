package br.gov.caixa.caixaverso.investimento.simulacao.data;

import br.gov.caixa.caixaverso.investimento.recomendacao.dto.ProdutoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.domain.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    /**
     * Filtra o produto adequado
     * Encontra o melhor produto (maior rentabilidade) que atende aos critérios.
     */
    Optional<Produto> findFirstByTipoAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeAnualDesc(
            String tipo, BigDecimal valor, Integer prazo);


    /**
     * retorna o DTO diretamente (Projeção).
     */
    @Query("SELECT new br.gov.caixa.caixaverso.investimento.recomendacao.dto.ProdutoDTO(" +
            "   p.id, p.nome, p.tipo, p.rentabilidadeAnual, p.risco) " +
            "FROM Produto p WHERE p.risco IN :riscos")
    List<ProdutoDTO> findRecomendadosPorRisco(List<String> riscos);

}
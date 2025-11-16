package br.gov.caixa.caixaverso.investimento.simulacao.mapper;

import br.gov.caixa.caixaverso.investimento.recomendacao.dto.ProdutoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.domain.Produto;
import org.springframework.stereotype.Component;

/**
 * Classe de Mapper (Clean Code / SRP).
 * Responsável por isolar a lógica de conversão entre a
 * Entidade 'Produto' e o 'ProdutoDTO'.
 */
@Component
public class ProdutoMapper {

    public ProdutoDTO toDTO(Produto produto) {
        if (produto == null) {
            return null;
        }

        return new ProdutoDTO(
                produto.getId(),
                produto.getNome(),
                produto.getTipo(),
                produto.getRentabilidadeAnual(),
                produto.getRisco()
        );
    }
}

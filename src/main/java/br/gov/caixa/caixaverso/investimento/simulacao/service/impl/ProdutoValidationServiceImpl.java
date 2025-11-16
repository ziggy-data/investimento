package br.gov.caixa.caixaverso.investimento.simulacao.service.impl;

import br.gov.caixa.caixaverso.investimento.exception.ValidacaoNegocioException;
import br.gov.caixa.caixaverso.investimento.simulacao.data.ProdutoRepository;
import br.gov.caixa.caixaverso.investimento.simulacao.domain.Produto;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoRequestDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.service.ProdutoValidationService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProdutoValidationServiceImpl implements ProdutoValidationService {

    private final ProdutoRepository produtoRepository;

    public ProdutoValidationServiceImpl(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    /**
     * Este @Cacheable está em um méto do public de um Bean
     * separado, então o Spring vai ativá-lo.
     */
    @Override
    @Cacheable(
            value = "validacaoProduto",
            key = "#request.tipoProduto().toLowerCase() + '-' + #request.valor() + '-' + #request.prazoMeses()"
    )
    public Produto validarProduto(SimulacaoRequestDTO request) {
        return produtoRepository.findFirstByTipoAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeAnualDesc(
                request.tipoProduto(),
                request.valor(),
                request.prazoMeses()
        ).orElseThrow(() ->
                new ValidacaoNegocioException(
                        String.format("Nenhum produto do tipo '%s' encontrado para o valor de R$ %.2f e prazo de %d meses.",
                                request.tipoProduto(), request.valor(), request.prazoMeses())
                )
        );
    }
}
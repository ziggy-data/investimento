package br.gov.caixa.caixaverso.investimento.simulacao.service;

import br.gov.caixa.caixaverso.investimento.simulacao.domain.Produto;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoRequestDTO;

public interface ProdutoValidationService {
    Produto validarProduto(SimulacaoRequestDTO request);
}

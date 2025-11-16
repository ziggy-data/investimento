package br.gov.caixa.caixaverso.investimento.recomendacao.service;

import br.gov.caixa.caixaverso.investimento.recomendacao.dto.ProdutoDTO;
import br.gov.caixa.caixaverso.investimento.recomendacao.dto.PerfilRiscoDTO;

import java.util.List;

public interface MotorRecomendacaoService {


    /**
     * Calcula o perfil de risco de um cliente com base no histórico.
     * @param clienteId O ID do cliente.
     * @return uma "promessa" do perfil de risco, executando em uma thread separada.
     */
    PerfilRiscoDTO calcularPerfilRisco(Long clienteId);

    /**
     * Busca produtos de investimento recomendados para um perfil de risco.
     * @param perfil O perfil (Conservador, Moderado, Agressivo).
     * @return Lista de produtos recomendados.
     */
    List<ProdutoDTO> recomendarProdutos(String perfil);
}

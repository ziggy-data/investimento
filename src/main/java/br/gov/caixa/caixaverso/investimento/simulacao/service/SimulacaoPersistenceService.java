package br.gov.caixa.caixaverso.investimento.simulacao.service;

import br.gov.caixa.caixaverso.investimento.simulacao.domain.Simulacao;

public interface SimulacaoPersistenceService {
    void persistirSimulacaoAsync(Simulacao simulacao);
}
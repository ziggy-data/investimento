package br.gov.caixa.caixaverso.investimento.simulacao.service;

import br.gov.caixa.caixaverso.investimento.simulacao.dto.HistoricoSimulacaoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.InvestimentoHistoricoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoAgregadaDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoRequestDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoResponseDTO;

import java.util.List;

public interface InvestimentoService {
    /**
     * Valida, calcula e persiste uma simulação de investimento.
     * @param requestDTO Dados da simulação.
     * @return DTO com o resultado da simulação.
     */
    SimulacaoResponseDTO simularEValidar(SimulacaoRequestDTO requestDTO);

    /**
     * Busca todas as simulações de investimento realizadas.
     * @return Lista com o histórico de simulações.
     */
    List<HistoricoSimulacaoDTO> buscarHistorico();

    /**
     * Busca dados agregados de simulações por produto e dia.
     * @return Lista de dados agregados.
     */
    List<SimulacaoAgregadaDTO> buscarSimulacoesAgregadas();

    /**
     * Busca um histórico de investimentos (mapeado de simulações) para um cliente.
     * @param clienteId O ID do cliente.
     * @return Lista de investimentos.
     */
    List<InvestimentoHistoricoDTO> buscarHistoricoInvestimentos(Long clienteId);
}

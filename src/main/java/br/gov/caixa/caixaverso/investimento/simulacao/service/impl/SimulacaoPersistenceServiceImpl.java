package br.gov.caixa.caixaverso.investimento.simulacao.service.impl;

import br.gov.caixa.caixaverso.investimento.simulacao.data.SimulacaoRepository;
import br.gov.caixa.caixaverso.investimento.simulacao.domain.Simulacao;
import br.gov.caixa.caixaverso.investimento.simulacao.service.SimulacaoPersistenceService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SimulacaoPersistenceServiceImpl implements SimulacaoPersistenceService {

    private final SimulacaoRepository simulacaoRepository;

    public SimulacaoPersistenceServiceImpl(SimulacaoRepository simulacaoRepository) {
        this.simulacaoRepository = simulacaoRepository;
    }

    /**
     * Este método rodará em uma thread do pool "taskExecutor".
     * O endpoint 'simularEValidar' não vai esperar ele terminar.
     * Além de salvar (Async), este método agora limpa o cache
     * "perfisDeRisco" para o clienteId específico.
     *
     * A próxima vez que o GET /perfil-risco for chamado, ele irá
     * ao banco (cache miss), calculará o novo perfil e salvará
     * o resultado atualizado no cache.
     */
    @Async("taskExecutor") // Usa o pool de threads do seu AsyncConfig
    @Transactional
    @Override
    @CacheEvict(value = "perfisDeRisco", key = "#simulacao.clienteId")
    public void persistirSimulacaoAsync(Simulacao simulacao) {
        // O @Transactional aqui garante que esta operação
        // de escrita tenha sua própria transação.
        simulacaoRepository.save(simulacao);
    }
}

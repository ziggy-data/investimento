package br.gov.caixa.caixaverso.investimento.simulacao.service.impl;

import br.gov.caixa.caixaverso.investimento.simulacao.dto.HistoricoSimulacaoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.InvestimentoHistoricoDTO;
import br.gov.caixa.caixaverso.investimento.recomendacao.dto.ProdutoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoAgregadaDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoRequestDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.dto.SimulacaoResponseDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.domain.Produto;
import br.gov.caixa.caixaverso.investimento.simulacao.domain.Simulacao;
import br.gov.caixa.caixaverso.investimento.simulacao.data.SimulacaoRepository;
import br.gov.caixa.caixaverso.investimento.simulacao.mapper.ProdutoMapper;
import br.gov.caixa.caixaverso.investimento.simulacao.service.InvestimentoService;
import br.gov.caixa.caixaverso.investimento.simulacao.service.ProdutoValidationService;
import br.gov.caixa.caixaverso.investimento.simulacao.service.SimulacaoPersistenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class InvestimentoServiceImpl implements InvestimentoService {

    private final SimulacaoRepository simulacaoRepository;
    private final ProdutoMapper produtoMapper;
    private final ProdutoValidationService produtoValidationService;
    private final SimulacaoPersistenceService simulacaoPersistenceService;

    // Injeção de dependência (Princípio 'D' do SOLID)
    public InvestimentoServiceImpl(SimulacaoRepository simulacaoRepository,
                                   ProdutoMapper produtoMapper,
                                   ProdutoValidationService produtoValidationService,
                                   SimulacaoPersistenceService simulacaoPersistenceService) {
        this.simulacaoRepository = simulacaoRepository;
        this.produtoMapper = produtoMapper;
        this.produtoValidationService = produtoValidationService;
        this.simulacaoPersistenceService = simulacaoPersistenceService;
    }

    @Override
    public SimulacaoResponseDTO simularEValidar(SimulacaoRequestDTO request) {

        // 1. Validar (Rápido - busca do cache)
        Produto produto = produtoValidationService.validarProduto(request);

        // 2. Calcular
        SimulacaoResponseDTO.ResultadoSimulacaoDTO resultado = calcularSimulacao(produto, request.valor(), request.prazoMeses());

        // 3. Preparar Persistência
        Simulacao simulacao = new Simulacao(
                request.clienteId(),
                produto,
                request.valor(),
                request.prazoMeses(),
                resultado.valorFinal()
        );

        // 4. Persistência Assíncrona
        simulacaoPersistenceService.persistirSimulacaoAsync(simulacao);

        // 5. Mapear (Rápido - CPU)
        ProdutoDTO produtoDTO = produtoMapper.toDTO(produto);

        // 6. Retornar ao usuário IMEDIATAMENTE
        return new SimulacaoResponseDTO(
                produtoDTO,
                resultado,
                simulacao.getDataSimulacao()
        );
    }

    private SimulacaoResponseDTO.ResultadoSimulacaoDTO calcularSimulacao(Produto produto, BigDecimal valor, int prazoMeses) {
        // Fórmula de juros simples (conforme exemplo do PDF: 10k -> 11.2k @ 12% a.a.)
        // (1 + (taxa_anual * (meses / 12.0)))

        BigDecimal prazoEmAnos = new BigDecimal(prazoMeses).divide(new BigDecimal(12), 8, RoundingMode.HALF_UP);
        BigDecimal rentabilidadeTotal = produto.getRentabilidadeAnual().multiply(prazoEmAnos);
        BigDecimal valorFinal = valor.multiply(BigDecimal.ONE.add(rentabilidadeTotal));

        // Arredondando para 2 casas decimais (boa prática financeira)
        valorFinal = valorFinal.setScale(2, RoundingMode.HALF_UP);

        return new SimulacaoResponseDTO.ResultadoSimulacaoDTO(
                valorFinal,
                produto.getRentabilidadeAnual(), // No exemplo, a rentabilidade efetiva foi a própria taxa
                prazoMeses
        );
    }

    /**
     * Implementação do método de busca de histórico.
     */
    @Override
    @Transactional(readOnly = true) // Boa prática: marca transações de leitura
    public List<HistoricoSimulacaoDTO> buscarHistorico() {
        return simulacaoRepository.findHistoricoCompleto();
    }

    /**
     * Implementação do método de agregação.
     * A lógica complexa está na query JPQL no repositório.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SimulacaoAgregadaDTO> buscarSimulacoesAgregadas() {
        return simulacaoRepository.findSimulacoesAgregadasPorProdutoEDia();
    }

    /**
     * Implementação do histórico de investimentos.
     */
    @Override
    @Transactional(readOnly = true)
    public List<InvestimentoHistoricoDTO> buscarHistoricoInvestimentos(Long clienteId) {
        return simulacaoRepository.findHistoricoInvestimentosPorClienteId(clienteId);
    }


}

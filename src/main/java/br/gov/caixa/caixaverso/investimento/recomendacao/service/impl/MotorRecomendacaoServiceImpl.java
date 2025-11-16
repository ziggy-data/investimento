package br.gov.caixa.caixaverso.investimento.recomendacao.service.impl;

import br.gov.caixa.caixaverso.investimento.recomendacao.domain.PerfilRisco;
import br.gov.caixa.caixaverso.investimento.recomendacao.dto.PerfilAgregadoDTO;
import br.gov.caixa.caixaverso.investimento.recomendacao.dto.ProdutoDTO;
import br.gov.caixa.caixaverso.investimento.recomendacao.dto.PerfilRiscoDTO;
import br.gov.caixa.caixaverso.investimento.simulacao.data.ProdutoRepository;
import br.gov.caixa.caixaverso.investimento.simulacao.data.SimulacaoRepository;
import br.gov.caixa.caixaverso.investimento.recomendacao.service.MotorRecomendacaoService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MotorRecomendacaoServiceImpl implements MotorRecomendacaoService {

    // Este novo serviço precisa de seus próprios repositórios
    private final ProdutoRepository produtoRepository;
    private final SimulacaoRepository simulacaoRepository;

    public MotorRecomendacaoServiceImpl(ProdutoRepository produtoRepository,
                                        SimulacaoRepository simulacaoRepository) {
        this.produtoRepository = produtoRepository;
        this.simulacaoRepository = simulacaoRepository;
    }

    /**
     * O método chama uma única query de agregação no banco
     * em vez de trazer todas as simulações para a memória.
     */
    @Override
    @Cacheable(value = "perfisDeRisco", key = "#clienteId")
    @Transactional(readOnly = true)
    public PerfilRiscoDTO calcularPerfilRisco(Long clienteId) {
        PerfilAgregadoDTO agregados = simulacaoRepository.findPerfilAgregadoPorCliente(clienteId);

        if (agregados.contagem() == 0) {
            return new PerfilRiscoDTO(clienteId, "Conservador", 0, "Sem dados históricos. Perfil padrão.");
        }

        // Pega os valores da query de agregação
        double mediaValor = agregados.mediaValor() != null ? agregados.mediaValor() : 0.0;
        double mediaRisco = agregados.mediaRisco() != null ? agregados.mediaRisco() : 0.0;
        int contagem = agregados.contagem().intValue();

        // A lógica de pontuação agora usa os valores agregados (muito mais rápido)
        int pontuacaoVolume = (mediaValor > 10000) ? 33 : (mediaValor > 5000) ? 20 : 10;
        int pontuacaoFreq = (contagem > 10) ? 33 : (contagem > 3) ? 20 : 10;
        int pontuacaoRisco = (int) Math.round(mediaRisco);

        int pontuacao = pontuacaoVolume + pontuacaoFreq + pontuacaoRisco;

        PerfilRisco perfil = PerfilRisco.fromPontuacao(pontuacao);

        // Construção do DTO
        return new PerfilRiscoDTO(
                clienteId,
                perfil.getNome(),        // Pega o nome do Enum
                pontuacao,
                perfil.getDescricao()    // Pega a descrição do Enum
        );
    }

    /**
     * 1. @Cacheable: Salva a lista no cache.
     * 2. Projeção: O repositório faz o mapeamento.
     */
    @Override
    @Cacheable(value = "produtosRecomendados", key = "#perfil.toLowerCase()")
    @Transactional(readOnly = true)
    public List<ProdutoDTO> recomendarProdutos(String perfil) {
        // 1. Converte o texto da API para o Enum de Domínio
        PerfilRisco perfilEnum = PerfilRisco.fromString(perfil);

        // 2. Pega a regra de negócio do Enum
        List<String> niveisRisco = perfilEnum.getNiveisRiscoAceitos();

        // 3. O Service apenas executa a busca
        return produtoRepository.findRecomendadosPorRisco(niveisRisco);
    }
}
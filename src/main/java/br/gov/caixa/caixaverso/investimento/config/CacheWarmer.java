package br.gov.caixa.caixaverso.investimento.config;

import br.gov.caixa.caixaverso.investimento.recomendacao.service.MotorRecomendacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Este componente "aquece" (preenche) os caches mais importantes
 * assim que a aplicação é iniciada, para que a primeira
 * requisição do usuário já seja ultra-rápida.
 */
@Component
public class CacheWarmer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(CacheWarmer.class);

    private final MotorRecomendacaoService motorRecomendacaoService;

    public CacheWarmer(MotorRecomendacaoService motorRecomendacaoService) {
        this.motorRecomendacaoService = motorRecomendacaoService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Iniciando aquecimento de caches...");

        try {
            // 1. Chama o método para "conservador". O @Cacheable vai
            // salvar o resultado no cache "produtosRecomendados".
            // A chamada agora retorna List<> (correto).
            logger.info("Aquecendo cache: produtosRecomendados::conservador");
            motorRecomendacaoService.recomendarProdutos("conservador");

            // 2. Chama para "moderado"
            logger.info("Aquecendo cache: produtosRecomendados::moderado");
            motorRecomendacaoService.recomendarProdutos("moderado");

            // 3. Chama para "agressivo"
            logger.info("Aquecendo cache: produtosRecomendados::agressivo");
            motorRecomendacaoService.recomendarProdutos("agressivo");

            logger.info("Aquecimento de caches concluído com sucesso.");

        } catch (Exception e) {
            logger.error("Falha ao aquecer os caches durante a inicialização.", e);
        }
    }
}

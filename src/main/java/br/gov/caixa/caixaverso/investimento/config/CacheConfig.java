package br.gov.caixa.caixaverso.investimento.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Define um gerenciador de cache.
     * Estamos usando um cache simples em memória (ConcurrentMap) que é
     * perfeito para guardar a lista de produtos recomendados.
     */
    @Bean
    public CacheManager cacheManager() {
        // Define os "Caches" que vamos usar.
        // O nome "produtosRecomendados" deve bater com o @Cacheable no Service.
        return new ConcurrentMapCacheManager("produtosRecomendados",
                "validacaoProduto", "perfisDeRisco");
    }
}

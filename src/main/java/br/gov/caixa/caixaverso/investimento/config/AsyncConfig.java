package br.gov.caixa.caixaverso.investimento.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    /**
     * Define um pool de threads customizado para ser usado com @Async.
     * Isso substitui o SimpleAsyncTaskExecutor padrão do Spring.
     *
     * @return um Executor de pool de threads.
     */
    @Bean(name = "taskExecutor") // Você pode dar o nome que quiser ao Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. Número de threads principais (core)
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(corePoolSize);

        // 2. Número máximo de threads
        // Um valor um pouco maior para lidar com picos, mas não infinito.
        executor.setMaxPoolSize(corePoolSize * 2);

        // 3. Capacidade da Fila
        // Quantas tarefas podem esperar na fila se todas as threads estiverem ocupadas.
        // Um valor alto (ex: 500) é bom para tarefas IO (como chamadas de API).
        // Um valor mais baixo é melhor para tarefas de CPU (como nossos cálculos).
        executor.setQueueCapacity(100);

        // 4. Prefixo dos Nomes das Threads
        // Essencial para debugging e logging (ex: "taskExecutor-1", "taskExecutor-2")
        executor.setThreadNamePrefix("taskExecutor-");

        // 5. Inicializa o executor
        executor.initialize();

        return executor;
    }
}

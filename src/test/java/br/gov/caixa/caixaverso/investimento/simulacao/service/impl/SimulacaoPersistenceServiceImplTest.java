package br.gov.caixa.caixaverso.investimento.simulacao.service.impl;

import br.gov.caixa.caixaverso.investimento.simulacao.data.SimulacaoRepository;
import br.gov.caixa.caixaverso.investimento.simulacao.domain.Simulacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException; // Exemplo de exceção do DB

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste Unitário - SimulacaoPersistenceService")
class SimulacaoPersistenceServiceImplTest {

    @Mock
    private SimulacaoRepository simulacaoRepository; // Nossa única dependência

    @InjectMocks
    private SimulacaoPersistenceServiceImpl persistenceService; // A classe sob teste

    // --- Teste de Sucesso ---

    @Test
    @DisplayName("Deve chamar repository.save() com a entidade Simulacao correta")
    void persistirSimulacaoAsync_DeveChamarSave_ComSimulacaoCorreta() {
        // Cenário (Arrange)
        Simulacao mockSimulacao = mock(Simulacao.class); // Um objeto de simulação

        // Ação (Act)
        // Como o @Async não funciona em teste unitário, ele roda sincronicamente
        persistenceService.persistirSimulacaoAsync(mockSimulacao);

        // Verificação (Assert)
        // Verificamos se o método 'save' foi chamado 1 vez com o objeto 'mockSimulacao'
        verify(simulacaoRepository, times(1)).save(mockSimulacao);
    }

    // --- Testes de Falha (Infraestrutura e Dados Incorretos) ---

    @Test
    @DisplayName("Deve propagar a exceção se o repository.save() falhar")
    void persistirSimulacaoAsync_DevePropagarExcecao_QuandoSaveFalhar() {
        // Cenário (Arrange) - (Falha: Infraestrutura)
        Simulacao mockSimulacao = mock(Simulacao.class);

        // Simula o repositório lançando uma exceção (ex: erro de banco)
        // Usamos 'doThrow' porque o método 'save' retorna void (no nosso caso)
        doThrow(new DataIntegrityViolationException("Erro de constraint"))
                .when(simulacaoRepository).save(mockSimulacao);

        // Ação & Verificação (Act & Assert)
        // Verificamos se a exceção é corretamente "re-lançada" pelo serviço
        assertThrows(DataIntegrityViolationException.class, () -> {
            persistenceService.persistirSimulacaoAsync(mockSimulacao);
        });

        // Garantimos que, mesmo com a falha, a tentativa de salvar ocorreu
        verify(simulacaoRepository, times(1)).save(mockSimulacao);
    }

    @Test
    @DisplayName("Deve chamar repository.save(null) se a simulação for nula")
    void persistirSimulacaoAsync_DeveChamarSaveComNull_QuandoInputForNulo() {
        // Cenário (Arrange) - (Falha: Dados Incorretos)
        Simulacao simulacaoNula = null;

        // Ação (Act)
        persistenceService.persistirSimulacaoAsync(simulacaoNula);

        // Verificação (Assert)
        // Verificamos se o serviço repassou o 'null' para o repositório
        verify(simulacaoRepository, times(1)).save(null);
    }
}
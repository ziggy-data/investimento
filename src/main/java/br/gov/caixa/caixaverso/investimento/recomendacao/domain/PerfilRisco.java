package br.gov.caixa.caixaverso.investimento.recomendacao.domain;

import br.gov.caixa.caixaverso.investimento.exception.ValidacaoNegocioException;
import lombok.Getter;

import java.util.List;
import java.util.stream.Stream;

/**
 * Representa os perfis de risco como um Enum (Princípio OCP).
 * Contém a regra de negócio de quais riscos cada perfil aceita.
 */
@Getter
public enum PerfilRisco {

    // --- CORREÇÃO: A lista de constantes DEVE vir primeiro ---
    CONSERVADOR(0, "Conservador", "Baixa movimentação, foco em liquidez.", List.of(Risco.BAIXO)),
    MODERADO(41, "Moderado", "Perfil equilibrado entre segurança e rentabilidade.", List.of(Risco.BAIXO, Risco.MODERADO)),
    AGRESSIVO(71, "Agressivo", "Busca por alta rentabilidade, maior risco.", List.of(Risco.BAIXO, Risco.MODERADO, Risco.ALTO));

    private static class Risco {
        private static final String BAIXO = "Baixo";
        private static final String MODERADO = "Moderado";
        private static final String ALTO = "Alto";
    }

    private final int minPontuacao;
    private final String nome;
    private final String descricao;
    private final List<String> niveisRiscoAceitos;

    PerfilRisco(int minPontuacao, String nome, String descricao, List<String> niveisRiscoAceitos) {
        this.minPontuacao = minPontuacao;
        this.nome = nome;
        this.descricao = descricao;
        this.niveisRiscoAceitos = niveisRiscoAceitos;
    }

    /**
     * OTIMIZAÇÃO OCP/SRP:
     * A lógica de negócio para determinar um perfil a partir da pontuação
     * vive AGORA DENTRO do domínio.
     */
    public static PerfilRisco fromPontuacao(int pontuacao) {
        // Itera do mais alto (Agressivo) para o mais baixo
        if (pontuacao >= AGRESSIVO.minPontuacao) {
            return AGRESSIVO;
        }
        if (pontuacao >= MODERADO.minPontuacao) {
            return MODERADO;
        }
        return CONSERVADOR;
    }

    /**
     * OTIMIZAÇÃO (Usada pelo 'recomendarProdutos'):
     * Encontra o Enum pelo nome.
     */
    public static PerfilRisco fromString(String perfilTexto) {
        if (perfilTexto == null) {
            throw new ValidacaoNegocioException("Perfil de risco não pode ser nulo.");
        }
        String nomeBusca = perfilTexto.trim();

        return Stream.of(values())
                .filter(perfil -> perfil.nome.equalsIgnoreCase(nomeBusca))
                .findFirst()
                .orElseThrow(() -> new ValidacaoNegocioException(
                        "Perfil de risco inválido: '" + perfilTexto + "'. Use Conservador, Moderado ou Agressivo.")
                );
    }
}
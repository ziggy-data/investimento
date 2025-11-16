package br.gov.caixa.caixaverso.investimento.recomendacao.dto;

/**
 * DTO para receber os dados agregados do perfil de risco direto do banco de dados.
 */
public record PerfilAgregadoDTO(
        Long contagem,
        Double mediaValor, // AVG(valorInvestido)
        Double mediaRisco  // AVG(pontuação de risco)
) {
    // Construtor padrão para o caso de não haver resultados (AVGs podem ser nulos)
    public PerfilAgregadoDTO {
        // Se o COUNT retornar nulo (o que não deve, mas por segurança),
        // nós garantimos que ele se torne 0.
        if (contagem == null) {
            contagem = 0L;
        }
    }
}
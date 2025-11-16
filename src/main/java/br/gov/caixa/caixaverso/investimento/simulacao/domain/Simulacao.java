package br.gov.caixa.caixaverso.investimento.simulacao.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "simulacoes", indexes = {
        @Index(name = "idx_simulacao_clienteid", columnList = "clienteId")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Simulacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clienteId;

    @ManyToOne
    private Produto produto;

    private BigDecimal valorInvestido;
    private BigDecimal valorFinal;
    private Integer prazoMeses;
    private Instant dataSimulacao;

    /**
     * OTIMIZAÇÃO (DDD - Rich Domain Model):
     * Este construtor garante que uma 'Simulacao' só pode ser
     * criada em um estado válido. A lógica de 'setDataSimulacao'
     * está encapsulada aqui.
     */
    public Simulacao(Long clienteId, Produto produto, BigDecimal valorInvestido,
                     Integer prazoMeses, BigDecimal valorFinal) {

        // Validações (Boa prática)
        if (clienteId == null) throw new IllegalArgumentException("ClienteId não pode ser nulo");
        if (produto == null) throw new IllegalArgumentException("Produto não pode ser nulo");

        this.clienteId = clienteId;
        this.produto = produto;
        this.valorInvestido = valorInvestido;
        this.prazoMeses = prazoMeses;
        this.valorFinal = valorFinal;
        this.dataSimulacao = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }
}

package br.gov.caixa.caixaverso.investimento.simulacao.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
@Getter
@Setter
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String tipo; // "CDB", "LCI", etc.
    private BigDecimal rentabilidadeAnual; // Ex: 0.12 para 12%
    private String risco; // "Baixo", "Moderado", "Alto"

    // Parâmetros para validação
    private BigDecimal valorMinimo;
    private Integer prazoMinimoMeses;
}
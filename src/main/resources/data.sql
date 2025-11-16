/* * Script para popular a tabela 'produtos' com dados de exemplo.
 * Os nomes das colunas aqui (snake_case) devem bater com o que o JPA/Hibernate
 * criou no banco de dados a partir da sua entidade 'Produto.java' (camelCase).
 */

INSERT INTO produtos (nome, tipo, rentabilidade_anual, risco, valor_minimo, prazo_minimo_meses)
VALUES
/* Produto 1: Este vai fazer sua simulação passar */
('CDB Caixa 2026', 'CDB', 0.12, 'Baixo', 1000.00, 12),

/* Produto 2: Opção de Risco Moderado (para o Perfil de Risco) */
('Fundo Caixa Moderado', 'Fundo', 0.15, 'Moderado', 2000.00, 24),

/* Produto 3: Um LCI (isento de IR, geralmente) */
('LCI Caixa Imobiliário 2027', 'LCI', 0.09, 'Baixo', 5000.00, 24),

/* Produto 4: Um Fundo mais arriscado */
('Fundo Caixa Agressivo', 'Fundo', 0.18, 'Alto', 5000.00, 36),

/* Produto 5: Tesouro Direto */
('Tesouro Selic 2029', 'Tesouro', 0.11, 'Baixo', 150.00, 1),

/* "FUNDO XPTO" */
('Fundo XPTO', 'Fundo', 0.32, 'Alto', 5000.00, 6),

/*  LCA  */
('LCA Caixa Agronegócio 2025', 'LCA', 0.085, 'Baixo', 3000.00, 12);
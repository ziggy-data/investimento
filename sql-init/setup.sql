IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'investimentoDB')
BEGIN
    CREATE DATABASE [investimentoDB];
    PRINT 'Banco de dados [investimentoDB] criado com sucesso.';
END
ELSE
BEGIN
    PRINT 'Banco de dados [investimentoDB] já existe.';
END;
GO -- <-- ADICIONE ISSO!

-- Agora, em um NOVO lote, podemos usar o banco
USE [investimentoDB];
GO -- <-- Boa prática adicionar isso também


-- Cria a tabela 'produtos' APENAS se ela não existir
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[produtos]') AND type in (N'U'))
BEGIN
    PRINT 'Criando a tabela [produtos]...';
    CREATE TABLE [dbo].[produtos] (
        [id] INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        [nome] NVARCHAR(100) NOT NULL,
        [tipo] NVARCHAR(50) NOT NULL,
        [rentabilidade_anual] DECIMAL(18, 4) NOT NULL,
        [risco] NVARCHAR(20) NOT NULL,
        [valor_minimo] DECIMAL(18, 2) NOT NULL,
        [prazo_minimo_meses] INT NOT NULL
    );
    PRINT 'Tabela [produtos] criada com sucesso.';
END
ELSE
BEGIN
    PRINT 'Tabela [produtos] já existe.';
END;
GO -- <-- ADICIONE ISSO!

-- -------------------------------------------------------------------
-- Insere os dados de exemplo APENAS se a tabela estiver vazia
-- -------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM [dbo].[produtos] WHERE [nome] = 'CDB Caixa 2026')
BEGIN
    PRINT 'Populando a tabela [produtos] com dados de exemplo...';
    
    INSERT INTO [dbo].[produtos] 
        (nome, tipo, rentabilidade_anual, risco, valor_minimo, prazo_minimo_meses)
    VALUES
    /* ... (seus dados aqui) ... */
    ('CDB Caixa 2026', 'CDB', 0.12, 'Baixo', 1000.00, 12),
    ('Fundo Caixa Moderado', 'Fundo', 0.15, 'Moderado', 2000.00, 24),
    ('LCI Caixa Imobiliário 2027', 'LCI', 0.09, 'Baixo', 5000.00, 24),
    ('Fundo Caixa Agressivo', 'Fundo', 0.18, 'Alto', 5000.00, 36),
    ('Tesouro Selic 2029', 'Tesouro', 0.11, 'Baixo', 150.00, 1),
    ('Fundo XPTO', 'Fundo', 0.32, 'Alto', 5000.00, 6),
    ('LCA Caixa Agronegócio 2025', 'LCA', 0.085, 'Baixo', 3000.00, 12);

    PRINT 'Dados de exemplo inseridos em [produtos].';
END
ELSE
BEGIN
    PRINT 'Dados de exemplo já existem em [produtos].';
END;
GO 
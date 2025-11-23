-- ==============================================
-- SCRIPT DE CRIAÇÃO DO BANCO DE DADOS
-- Sistema de Gestão de Cartões
-- PostgreSQL - Proxmox Container
-- ==============================================

-- Configurações iniciais
SET timezone = 'America/Sao_Paulo';
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ==============================================
-- 1. TABELA PESSOAS (Classe Base)
-- ==============================================
CREATE TABLE IF NOT EXISTS pessoas (
    id BIGSERIAL PRIMARY KEY,
    tipo_pessoa VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'ATIVO',
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observacoes VARCHAR(500),
    
    -- Campos de Endereço
    cep VARCHAR(8),
    logradouro VARCHAR(150),
    numero VARCHAR(10),
    complemento VARCHAR(50),
    bairro VARCHAR(50),
    cidade VARCHAR(50),
    uf VARCHAR(2),
    
    -- Campos de Contato
    email_principal VARCHAR(100),
    telefone_principal VARCHAR(20),
    celular_principal VARCHAR(20)
);

-- Índices para Pessoas
CREATE INDEX IF NOT EXISTS idx_pessoas_status ON pessoas(status);
CREATE INDEX IF NOT EXISTS idx_pessoas_tipo ON pessoas(tipo_pessoa);
CREATE INDEX IF NOT EXISTS idx_pessoas_email ON pessoas(email_principal);

-- ==============================================
-- 2. TABELA CLIENTES (Herda de Pessoas)
-- ==============================================
CREATE TABLE IF NOT EXISTS clientes (
    id BIGSERIAL PRIMARY KEY,
    pessoa_id BIGINT NOT NULL REFERENCES pessoas(id) ON DELETE CASCADE,
    tipo_cliente VARCHAR(20) NOT NULL,
    
    -- Campos específicos de Cliente
    codigo_cliente VARCHAR(20) UNIQUE,
    categoria VARCHAR(20) DEFAULT 'STANDARD',
    limite_credito DECIMAL(15,2) DEFAULT 0.00,
    limite_credito_utilizado DECIMAL(15,2) DEFAULT 0.00,
    renda_mensal DECIMAL(15,2) DEFAULT 0.00,
    status_credito VARCHAR(30) DEFAULT 'PENDENTE_ANALISE',
    observacoes VARCHAR(500),
    data_ultima_analise_credito TIMESTAMP,
    data_inicio_relacionamento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tempo_relacionamento_meses INTEGER DEFAULT 0,
    score_credito INTEGER,
    classificacao_risco VARCHAR(20) DEFAULT 'NAO_CLASSIFICADO',
    
    -- Campos para cartões adicionais
    solicitou_cartao_adicional BOOLEAN DEFAULT FALSE,
    quantidade_cartoes_adicionais INTEGER DEFAULT 0,
    data_ultima_solicitacao_adicional TIMESTAMP,
    tipo_beneficiario_adicional VARCHAR(50),
    nome_beneficiario_adicional VARCHAR(100),
    limite_solicitado_adicional DECIMAL(15,2) DEFAULT 0.00
);

-- Índices para Clientes
CREATE INDEX IF NOT EXISTS idx_clientes_pessoa ON clientes(pessoa_id);
CREATE INDEX IF NOT EXISTS idx_clientes_codigo ON clientes(codigo_cliente);
CREATE INDEX IF NOT EXISTS idx_clientes_categoria ON clientes(categoria);
CREATE INDEX IF NOT EXISTS idx_clientes_status_credito ON clientes(status_credito);

-- ==============================================
-- 3. TABELA CLIENTES_PF (Pessoa Física)
-- ==============================================
CREATE TABLE IF NOT EXISTS clientes_pf (
    cliente_id BIGINT PRIMARY KEY REFERENCES clientes(id) ON DELETE CASCADE,
    nome VARCHAR(100) NOT NULL,
    sobrenome VARCHAR(100) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    data_nascimento DATE NOT NULL,
    nacionalidade VARCHAR(50) DEFAULT 'Brasileira',
    estado_civil VARCHAR(50),
    sexo VARCHAR(20),
    profissao VARCHAR(100)
);

-- Índices para ClientesPF
CREATE INDEX IF NOT EXISTS idx_clientes_pf_cpf ON clientes_pf(cpf);
CREATE INDEX IF NOT EXISTS idx_clientes_pf_nome ON clientes_pf(nome, sobrenome);

-- ==============================================
-- 4. TABELA CLIENTES_PJ (Pessoa Jurídica)
-- ==============================================
CREATE TABLE IF NOT EXISTS clientes_pj (
    cliente_id BIGINT PRIMARY KEY REFERENCES clientes(id) ON DELETE CASCADE,
    razao_social VARCHAR(200) NOT NULL,
    nome_fantasia VARCHAR(200),
    cnpj VARCHAR(14) NOT NULL UNIQUE,
    inscricao_estadual VARCHAR(20),
    inscricao_municipal VARCHAR(20),
    data_fundacao DATE,
    porte_empresa VARCHAR(20),
    regime_tributario VARCHAR(30),
    atividade_principal VARCHAR(200),
    capital_social DECIMAL(15,2)
);

-- Índices para ClientesPJ
CREATE INDEX IF NOT EXISTS idx_clientes_pj_cnpj ON clientes_pj(cnpj);
CREATE INDEX IF NOT EXISTS idx_clientes_pj_razao_social ON clientes_pj(razao_social);

-- ==============================================
-- 5. TABELA CONTRATOS
-- ==============================================
CREATE TABLE IF NOT EXISTS contratos (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    numero_contrato VARCHAR(30) NOT NULL UNIQUE,
    tipo_contrato VARCHAR(30) NOT NULL,
    status VARCHAR(20) DEFAULT 'ATIVO',
    data_inicio DATE NOT NULL,
    data_fim DATE,
    data_vencimento DATE,
    valor_contrato DECIMAL(15,2),
    taxa_juros DECIMAL(5,4),
    dia_vencimento INTEGER CHECK (dia_vencimento >= 1 AND dia_vencimento <= 31),
    renovacao_automatica BOOLEAN DEFAULT FALSE,
    observacoes VARCHAR(500),
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para Contratos
CREATE INDEX IF NOT EXISTS idx_contratos_cliente ON contratos(cliente_id);
CREATE INDEX IF NOT EXISTS idx_contratos_numero ON contratos(numero_contrato);
CREATE INDEX IF NOT EXISTS idx_contratos_status ON contratos(status);
CREATE INDEX IF NOT EXISTS idx_contratos_tipo ON contratos(tipo_contrato);

-- ==============================================
-- 6. TABELA CARTÕES (Classe Base)
-- ==============================================
CREATE TABLE IF NOT EXISTS cartoes (
    id BIGSERIAL PRIMARY KEY,
    tipo_cartao VARCHAR(20) NOT NULL,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    contrato_id BIGINT NOT NULL REFERENCES contratos(id) ON DELETE CASCADE,
    
    -- Campos básicos do cartão
    numero_cartao VARCHAR(16) NOT NULL UNIQUE,
    nome_portador VARCHAR(100) NOT NULL,
    data_validade DATE NOT NULL,
    cvv VARCHAR(4) NOT NULL,
    status VARCHAR(20) DEFAULT 'ATIVO',
    limite DECIMAL(15,2),
    limite_utilizado DECIMAL(15,2) DEFAULT 0.00,
    bandeira VARCHAR(20) NOT NULL,
    principal BOOLEAN DEFAULT FALSE,
    contactless BOOLEAN DEFAULT FALSE,
    data_emissao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observacoes VARCHAR(500),
    tentativas_erradas INTEGER DEFAULT 0,
    data_bloqueio TIMESTAMP
);

-- Índices para Cartões
CREATE INDEX IF NOT EXISTS idx_cartoes_cliente ON cartoes(cliente_id);
CREATE INDEX IF NOT EXISTS idx_cartoes_contrato ON cartoes(contrato_id);
CREATE INDEX IF NOT EXISTS idx_cartoes_numero ON cartoes(numero_cartao);
CREATE INDEX IF NOT EXISTS idx_cartoes_status ON cartoes(status);
CREATE INDEX IF NOT EXISTS idx_cartoes_tipo ON cartoes(tipo_cartao);
CREATE INDEX IF NOT EXISTS idx_cartoes_bandeira ON cartoes(bandeira);

-- ==============================================
-- 7. TABELAS ESPECÍFICAS DE CARTÕES
-- ==============================================

-- Cartão de Crédito
CREATE TABLE IF NOT EXISTS cartao_credito (
    cartao_id BIGINT PRIMARY KEY REFERENCES cartoes(id) ON DELETE CASCADE,
    limite_rotativo DECIMAL(15,2),
    taxa_juros_rotativo DECIMAL(5,4),
    taxa_juros_parcelamento DECIMAL(5,4),
    limite_parcelamento DECIMAL(15,2),
    anuidade DECIMAL(10,2),
    data_vencimento_fatura INTEGER,
    programa_pontos VARCHAR(100),
    milhas VARCHAR(100)
);

-- Cartão de Débito
CREATE TABLE IF NOT EXISTS cartao_debito (
    cartao_id BIGINT PRIMARY KEY REFERENCES cartoes(id) ON DELETE CASCADE,
    conta_id BIGINT,
    limite_diario DECIMAL(15,2),
    limite_saque DECIMAL(15,2),
    taxa_saque DECIMAL(5,4),
    taxa_transferencia DECIMAL(5,4)
);

-- Cartão Alimentação
CREATE TABLE IF NOT EXISTS cartao_alimentacao (
    cartao_id BIGINT PRIMARY KEY REFERENCES cartoes(id) ON DELETE CASCADE,
    saldo DECIMAL(15,2),
    limite_diario DECIMAL(15,2),
    estabelecimentos_aceitos TEXT,
    taxa_administracao DECIMAL(5,4),
    data_vencimento_saldo DATE
);

-- Cartão Refeição
CREATE TABLE IF NOT EXISTS cartao_refeicao (
    cartao_id BIGINT PRIMARY KEY REFERENCES cartoes(id) ON DELETE CASCADE,
    saldo DECIMAL(15,2),
    limite_diario DECIMAL(15,2),
    estabelecimentos_aceitos TEXT,
    taxa_administracao DECIMAL(5,4),
    data_vencimento_saldo DATE
);

-- Cartão Combustível
CREATE TABLE IF NOT EXISTS cartao_combustivel (
    cartao_id BIGINT PRIMARY KEY REFERENCES cartoes(id) ON DELETE CASCADE,
    limite_mensal DECIMAL(15,2),
    limite_diario DECIMAL(15,2),
    postos_aceitos TEXT,
    tipo_combustivel VARCHAR(50),
    desconto_por_litro DECIMAL(5,4)
);

-- Cartão Saúde
CREATE TABLE IF NOT EXISTS cartao_saude (
    cartao_id BIGINT PRIMARY KEY REFERENCES cartoes(id) ON DELETE CASCADE,
    saldo DECIMAL(15,2),
    limite_anual DECIMAL(15,2),
    estabelecimentos_aceitos TEXT,
    cobertura_medica VARCHAR(200),
    coparticipacao DECIMAL(5,4)
);

-- ==============================================
-- 8. TABELA ENDEREÇOS
-- ==============================================
CREATE TABLE IF NOT EXISTS enderecos (
    id BIGSERIAL PRIMARY KEY,
    pessoa_id BIGINT NOT NULL REFERENCES pessoas(id) ON DELETE CASCADE,
    tipo VARCHAR(20) DEFAULT 'RESIDENCIAL',
    cep VARCHAR(8),
    logradouro VARCHAR(150),
    numero VARCHAR(10),
    complemento VARCHAR(50),
    bairro VARCHAR(50),
    cidade VARCHAR(50),
    uf VARCHAR(2),
    pais VARCHAR(50) DEFAULT 'Brasil',
    principal BOOLEAN DEFAULT FALSE
);

-- Índices para Endereços
CREATE INDEX IF NOT EXISTS idx_enderecos_pessoa ON enderecos(pessoa_id);
CREATE INDEX IF NOT EXISTS idx_enderecos_principal ON enderecos(principal);
CREATE INDEX IF NOT EXISTS idx_enderecos_cep ON enderecos(cep);

-- ==============================================
-- 9. TABELA CONTATOS
-- ==============================================
CREATE TABLE IF NOT EXISTS contatos (
    id BIGSERIAL PRIMARY KEY,
    pessoa_id BIGINT NOT NULL REFERENCES pessoas(id) ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL,
    valor VARCHAR(100) NOT NULL,
    principal BOOLEAN DEFAULT FALSE,
    ativo BOOLEAN DEFAULT TRUE,
    observacoes VARCHAR(500),
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para Contatos
CREATE INDEX IF NOT EXISTS idx_contatos_pessoa ON contatos(pessoa_id);
CREATE INDEX IF NOT EXISTS idx_contatos_tipo ON contatos(tipo);
CREATE INDEX IF NOT EXISTS idx_contatos_principal ON contatos(principal);
CREATE INDEX IF NOT EXISTS idx_contatos_ativo ON contatos(ativo);

-- ==============================================
-- 10. TABELA PESSOA_CONTATOS_ADICIONAIS
-- ==============================================
CREATE TABLE IF NOT EXISTS pessoa_contatos_adicionais (
    pessoa_id BIGINT NOT NULL REFERENCES pessoas(id) ON DELETE CASCADE,
    contato VARCHAR(100) NOT NULL,
    PRIMARY KEY (pessoa_id, contato)
);

-- ==============================================
-- 11. TABELAS AUXILIARES
-- ==============================================

-- Tabela Usuários
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    pessoa_id BIGINT REFERENCES pessoas(id) ON DELETE SET NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultimo_acesso TIMESTAMP,
    tentativas_login INTEGER DEFAULT 0,
    bloqueado BOOLEAN DEFAULT FALSE
);

-- Tabela Perfil
CREATE TABLE IF NOT EXISTS perfil (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao VARCHAR(200),
    tipo_padrao VARCHAR(20),
    ativo BOOLEAN DEFAULT TRUE,
    permissoes TEXT
);

-- Tabela Banco
CREATE TABLE IF NOT EXISTS banco (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL UNIQUE,
    nome VARCHAR(200) NOT NULL,
    cnpj VARCHAR(14),
    ativo BOOLEAN DEFAULT TRUE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela Conta
CREATE TABLE IF NOT EXISTS conta (
    id BIGSERIAL PRIMARY KEY,
    banco_id BIGINT REFERENCES banco(id),
    cliente_id BIGINT REFERENCES clientes(id),
    numero_conta VARCHAR(20) NOT NULL,
    agencia VARCHAR(10),
    tipo_conta VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ATIVA',
    saldo DECIMAL(15,2) DEFAULT 0.00,
    limite_credito DECIMAL(15,2) DEFAULT 0.00,
    data_abertura DATE DEFAULT CURRENT_DATE
);

-- ==============================================
-- 12. TRIGGERS PARA ATUALIZAÇÃO AUTOMÁTICA
-- ==============================================

-- Trigger para atualizar data_atualizacao em pessoas
CREATE OR REPLACE FUNCTION update_pessoas_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_pessoas_timestamp
    BEFORE UPDATE ON pessoas
    FOR EACH ROW
    EXECUTE FUNCTION update_pessoas_timestamp();

-- Trigger para atualizar data_atualizacao em contratos
CREATE OR REPLACE FUNCTION update_contratos_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_contratos_timestamp
    BEFORE UPDATE ON contratos
    FOR EACH ROW
    EXECUTE FUNCTION update_contratos_timestamp();

-- Trigger para atualizar data_atualizacao em cartoes
CREATE OR REPLACE FUNCTION update_cartoes_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_cartoes_timestamp
    BEFORE UPDATE ON cartoes
    FOR EACH ROW
    EXECUTE FUNCTION update_cartoes_timestamp();

-- Trigger para atualizar data_atualizacao em contatos
CREATE OR REPLACE FUNCTION update_contatos_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_contatos_timestamp
    BEFORE UPDATE ON contatos
    FOR EACH ROW
    EXECUTE FUNCTION update_contatos_timestamp();

-- ==============================================
-- 13. DADOS INICIAIS (SEED DATA)
-- ==============================================

-- Inserir perfis padrão
INSERT INTO perfil (nome, descricao, tipo_padrao, ativo) VALUES
('ADMIN', 'Administrador do Sistema', 'ADMIN', true),
('GERENTE', 'Gerente de Cartões', 'GERENTE', true),
('ANALISTA', 'Analista de Crédito', 'ANALISTA', true),
('ATENDENTE', 'Atendente', 'ATENDENTE', true)
ON CONFLICT (nome) DO NOTHING;

-- Inserir bancos principais
INSERT INTO banco (codigo, nome, cnpj, ativo) VALUES
('001', 'Banco do Brasil S.A.', '00000000000191', true),
('104', 'Caixa Econômica Federal', '00360305000104', true),
('341', 'Itaú Unibanco S.A.', '60746948000112', true),
('033', 'Santander (Brasil) S.A.', '90400888000142', true),
('237', 'Banco Bradesco S.A.', '60746948000112', true),
('260', 'Nu Pagamentos S.A.', '18236120000158', true)
ON CONFLICT (codigo) DO NOTHING;

-- ==============================================
-- 14. COMENTÁRIOS DAS TABELAS
-- ==============================================

COMMENT ON TABLE pessoas IS 'Tabela base para todas as pessoas do sistema';
COMMENT ON TABLE clientes IS 'Clientes que podem ter cartões';
COMMENT ON TABLE clientes_pf IS 'Clientes Pessoa Física';
COMMENT ON TABLE clientes_pj IS 'Clientes Pessoa Jurídica';
COMMENT ON TABLE contratos IS 'Contratos de cartões';
COMMENT ON TABLE cartoes IS 'Cartões emitidos';
COMMENT ON TABLE enderecos IS 'Endereços das pessoas';
COMMENT ON TABLE contatos IS 'Contatos das pessoas';
COMMENT ON TABLE usuarios IS 'Usuários do sistema';
COMMENT ON TABLE perfil IS 'Perfis de acesso';
COMMENT ON TABLE banco IS 'Bancos parceiros';
COMMENT ON TABLE conta IS 'Contas bancárias';

-- ==============================================
-- 15. VERIFICAÇÃO FINAL
-- ==============================================

-- Verificar se todas as tabelas foram criadas
DO $$
DECLARE
    table_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO table_count
    FROM information_schema.tables
    WHERE table_schema = 'public'
    AND table_name IN (
        'pessoas', 'clientes', 'clientes_pf', 'clientes_pj',
        'contratos', 'cartoes', 'cartao_credito', 'cartao_debito',
        'cartao_alimentacao', 'cartao_refeicao', 'cartao_combustivel',
        'cartao_saude', 'enderecos', 'contatos', 'usuarios',
        'perfil', 'banco', 'conta', 'pessoa_contatos_adicionais'
    );
    
    RAISE NOTICE 'Total de tabelas criadas: %', table_count;
    
    IF table_count >= 19 THEN
        RAISE NOTICE '✅ Estrutura do banco criada com sucesso!';
    ELSE
        RAISE NOTICE '⚠️  Algumas tabelas podem não ter sido criadas. Verifique os logs.';
    END IF;
END $$;

-- Log final
DO $$
BEGIN
    RAISE NOTICE '🎉 Script de criação do banco executado com sucesso!';
    RAISE NOTICE '📊 Sistema de Gestão de Cartões pronto para uso';
    RAISE NOTICE '🔗 Conecte a aplicação Spring Boot ao banco PostgreSQL';
END $$;



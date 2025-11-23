-- ==============================================
-- SCRIPT DE ATUALIZAÇÃO DE TABELAS DE USUÁRIOS E PERFIS
-- Sistema de Gestão de Cartões
-- ==============================================

-- Atualizar tabela perfis se necessário
DO $$
BEGIN
    -- Adicionar coluna nivel_acesso se não existir
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'perfis' AND column_name = 'nivel_acesso') THEN
        ALTER TABLE perfis ADD COLUMN nivel_acesso INTEGER NOT NULL DEFAULT 1;
    END IF;

    -- Adicionar coluna ativo se não existir
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'perfis' AND column_name = 'ativo') THEN
        ALTER TABLE perfis ADD COLUMN ativo BOOLEAN DEFAULT true;
    END IF;

    -- Adicionar coluna data_criacao se não existir
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'perfis' AND column_name = 'data_criacao') THEN
        ALTER TABLE perfis ADD COLUMN data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;

    -- Adicionar coluna data_atualizacao se não existir
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'perfis' AND column_name = 'data_atualizacao') THEN
        ALTER TABLE perfis ADD COLUMN data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;

-- Atualizar tabela usuarios se necessário
DO $$
BEGIN
    -- Adicionar coluna bloqueado se não existir
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'usuarios' AND column_name = 'bloqueado') THEN
        ALTER TABLE usuarios ADD COLUMN bloqueado BOOLEAN DEFAULT false;
    END IF;

    -- Adicionar coluna tentativas_login se não existir
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'usuarios' AND column_name = 'tentativas_login') THEN
        ALTER TABLE usuarios ADD COLUMN tentativas_login INTEGER DEFAULT 0;
    END IF;
END $$;

-- Criar tabela de permissões se não existir
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'permissoes') THEN
        CREATE TABLE permissoes (
            id BIGSERIAL PRIMARY KEY,
            nome VARCHAR(100) NOT NULL UNIQUE,
            descricao VARCHAR(255),
            modulo VARCHAR(50) NOT NULL,
            data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
    END IF;
END $$;

-- Criar tabela de relacionamento perfil_permissoes se não existir
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'perfil_permissoes') THEN
        CREATE TABLE perfil_permissoes (
            perfil_id BIGINT NOT NULL REFERENCES perfis(id) ON DELETE CASCADE,
            permissao_id BIGINT NOT NULL REFERENCES permissoes(id) ON DELETE CASCADE,
            PRIMARY KEY (perfil_id, permissao_id)
        );
    END IF;
END $$;

-- Criar tabela de relacionamento usuario_perfis se não existir
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'usuario_perfis') THEN
        CREATE TABLE usuario_perfis (
            usuario_id BIGINT NOT NULL REFERENCES usuarios(pessoa_id) ON DELETE CASCADE,
            perfil_id BIGINT NOT NULL REFERENCES perfis(id) ON DELETE CASCADE,
            data_atribuicao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            PRIMARY KEY (usuario_id, perfil_id)
        );
    END IF;
END $$;

-- Criar índices necessários
DO $$
BEGIN
    -- Índice para perfis
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_perfis_nome') THEN
        CREATE INDEX idx_perfis_nome ON perfis(nome);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_perfis_nivel') THEN
        CREATE INDEX idx_perfis_nivel ON perfis(nivel_acesso);
    END IF;

    -- Índice para permissões
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_permissoes_nome') THEN
        CREATE INDEX idx_permissoes_nome ON permissoes(nome);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_permissoes_modulo') THEN
        CREATE INDEX idx_permissoes_modulo ON permissoes(modulo);
    END IF;

    -- Índice para usuários
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_usuarios_username') THEN
        CREATE INDEX idx_usuarios_username ON usuarios(username);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_usuarios_status') THEN
        CREATE INDEX idx_usuarios_status ON usuarios(bloqueado);
    END IF;

    -- Índices para relacionamentos
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_usuario_perfis_usuario') THEN
        CREATE INDEX idx_usuario_perfis_usuario ON usuario_perfis(usuario_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_usuario_perfis_perfil') THEN
        CREATE INDEX idx_usuario_perfis_perfil ON usuario_perfis(perfil_id);
    END IF;
END $$;

-- Inserir permissões padrão se não existirem
DO $$
DECLARE
    permissao RECORD;
BEGIN
    FOR permissao IN VALUES 
        ('ACESSO_SISTEMA', 'Acesso ao Sistema', 'SISTEMA'),
        ('GERENCIAR_USUARIOS', 'Gerenciar Usuários', 'SISTEMA'),
        ('GERENCIAR_PERFIS', 'Gerenciar Perfis', 'SISTEMA'),
        ('AUDITAR_SISTEMA', 'Auditar Sistema', 'SISTEMA'),
        ('VISUALIZAR_CARTOES', 'Visualizar Cartões', 'CARTOES'),
        ('CADASTRAR_CARTAO', 'Cadastrar Cartão', 'CARTOES'),
        ('ALTERAR_CARTAO', 'Alterar Cartão', 'CARTOES'),
        ('BLOQUEAR_CARTAO', 'Bloquear Cartão', 'CARTOES'),
        ('LIBERAR_CARTAO', 'Liberar Cartão', 'CARTOES'),
        ('CANCELAR_CARTAO', 'Cancelar Cartão', 'CARTOES'),
        ('ALTERAR_LIMITE_CARTAO', 'Alterar Limite de Cartão', 'CARTOES'),
        ('VISUALIZAR_CLIENTES', 'Visualizar Clientes', 'CLIENTES'),
        ('CADASTRAR_CLIENTE', 'Cadastrar Cliente', 'CLIENTES'),
        ('ALTERAR_CLIENTE', 'Alterar Cliente', 'CLIENTES'),
        ('CANCELAR_CLIENTE', 'Cancelar Cliente', 'CLIENTES'),
        ('ANALISAR_CREDITO', 'Análise de Crédito', 'CLIENTES'),
        ('LIBERAR_LIMITE_CLIENTE', 'Liberar Limite de Cliente', 'CLIENTES'),
        ('GERAR_RELATORIOS', 'Gerar Relatórios', 'RELATORIOS'),
        ('EXPORTAR_DADOS', 'Exportar Dados', 'RELATORIOS'),
        ('VISUALIZAR_RELATORIOS_SENSIVEIS', 'Visualizar Relatórios Sensíveis', 'RELATORIOS'),
        ('APROVAR_TRANSACAO', 'Aprovar Transação', 'FINANCEIRO'),
        ('LIBERAR_CREDITO', 'Liberar Crédito', 'FINANCEIRO'),
        ('CONFIGURAR_TAXAS', 'Configurar Taxas', 'FINANCEIRO'),
        ('VISUALIZAR_CONTRATOS', 'Visualizar Contratos', 'CONTRATOS'),
        ('CADASTRAR_CONTRATO', 'Cadastrar Contrato', 'CONTRATOS'),
        ('ALTERAR_CONTRATO', 'Alterar Contrato', 'CONTRATOS'),
        ('CANCELAR_CONTRATO', 'Cancelar Contrato', 'CONTRATOS'),
        ('VISUALIZAR_FATURAS', 'Visualizar Faturas', 'FATURAS'),
        ('GERAR_FATURA', 'Gerar Fatura', 'FATURAS'),
        ('PAGAR_FATURA', 'Pagar Fatura', 'FATURAS')
    LOOP
        IF NOT EXISTS (SELECT 1 FROM permissoes WHERE nome = permissao.nome) THEN
            INSERT INTO permissoes (nome, descricao, modulo) 
            VALUES (permissao.nome, permissao.descricao, permissao.modulo);
        END IF;
    END LOOP;
END $$;

-- Inserir perfis padrão se não existirem
DO $$
DECLARE
    perfil RECORD;
BEGIN
    FOR perfil IN VALUES 
        ('ADMIN', 'Administrador do Sistema', 10),
        ('SUPERINTENDENTE', 'Superintendente de Operações', 9),
        ('GERENTE_CARTOES', 'Gerente de Cartões', 8),
        ('GERENTE_CLIENTES', 'Gerente de Clientes', 7),
        ('SUPERVISOR_ATENDIMENTO', 'Supervisor de Atendimento', 6),
        ('ANALISTA_RISCO', 'Analista de Risco', 5),
        ('ATENDENTE_CARTOES', 'Atendente de Cartões', 4),
        ('OPERADOR_CARTOES', 'Operador de Cartões', 3),
        ('CLIENTE_PF', 'Cliente Pessoa Física', 2),
        ('CLIENTE_PJ', 'Cliente Pessoa Jurídica', 2),
        ('AUDITOR', 'Auditor Interno', 9)
    LOOP
        IF NOT EXISTS (SELECT 1 FROM perfis WHERE nome = perfil.nome) THEN
            INSERT INTO perfis (nome, descricao, nivel_acesso, ativo, data_criacao, data_atualizacao) 
            VALUES (perfil.nome, perfil.descricao, perfil.nivel_acesso, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
        END IF;
    END LOOP;
END $$;

-- Atribuir permissões aos perfis padrão
DO $$
DECLARE
    perfil RECORD;
    permissao RECORD;
BEGIN
    -- Atribuir todas as permissões ao perfil ADMIN
    FOR permissao IN VALUES 
        ('ACESSO_SISTEMA', 'GERENCIAR_USUARIOS', 'GERENCIAR_PERFIS', 'AUDITAR_SISTEMA',
         'VISUALIZAR_CARTOES', 'CADASTRAR_CARTAO', 'ALTERAR_CARTAO', 'BLOQUEAR_CARTAO',
         'LIBERAR_CARTAO', 'CANCELAR_CARTAO', 'ALTERAR_LIMITE_CARTAO',
         'VISUALIZAR_CLIENTES', 'CADASTRAR_CLIENTE', 'ALTERAR_CLIENTE', 'CANCELAR_CLIENTE',
         'ANALISAR_CREDITO', 'LIBERAR_LIMITE_CLIENTE',
         'GERAR_RELATORIOS', 'EXPORTAR_DADOS', 'VISUALIZAR_RELATORIOS_SENSIVEIS',
         'APROVAR_TRANSACAO', 'LIBERAR_CREDITO', 'CONFIGURAR_TAXAS',
         'VISUALIZAR_CONTRATOS', 'CADASTRAR_CONTRATO', 'ALTERAR_CONTRATO', 'CANCELAR_CONTRATO',
         'VISUALIZAR_FATURAS', 'GERAR_FATURA', 'PAGAR_FATURA')
    LOOP
        INSERT INTO perfil_permissoes (perfil_id, permissao_id)
        SELECT perfis.id, permissoes.id
        FROM perfis, permissoes
        WHERE perfis.nome = 'ADMIN' AND permissoes.nome = permissao.nome
        ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
    END LOOP;

    -- Atribuir permissões ao perfil SUPERINTENDENTE
    FOR permissao IN VALUES 
        ('ACESSO_SISTEMA', 'AUDITAR_SISTEMA',
         'VISUALIZAR_CARTOES', 'ALTERAR_CARTAO', 'LIBERAR_CARTAO', 'ALTERAR_LIMITE_CARTAO',
         'VISUALIZAR_CLIENTES', 'ALTERAR_CLIENTE', 'ANALISAR_CREDITO', 'LIBERAR_LIMITE_CLIENTE',
         'GERAR_RELATORIOS', 'EXPORTAR_DADOS', 'VISUALIZAR_RELATORIOS_SENSIVEIS',
         'VISUALIZAR_CONTRATOS', 'ALTERAR_CONTRATO',
         'VISUALIZAR_FATURAS', 'GERAR_FATURA')
    LOOP
        INSERT INTO perfil_permissoes (perfil_id, permissao_id)
        SELECT perfis.id, permissoes.id
        FROM perfis, permissoes
        WHERE perfis.nome = 'SUPERINTENDENTE' AND permissoes.nome = permissao.nome
        ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
    END LOOP;

    -- Atribuir permissões ao perfil GERENTE_CARTOES
    FOR permissao IN VALUES 
        ('ACESSO_SISTEMA',
         'VISUALIZAR_CARTOES', 'CADASTRAR_CARTAO', 'ALTERAR_CARTAO', 'BLOQUEAR_CARTAO',
         'LIBERAR_CARTAO', 'ALTERAR_LIMITE_CARTAO',
         'VISUALIZAR_CLIENTES', 'ALTERAR_CLIENTE',
         'GERAR_RELATORIOS',
         'VISUALIZAR_CONTRATOS',
         'VISUALIZAR_FATURAS', 'GERAR_FATURA')
    LOOP
        INSERT INTO perfil_permissoes (perfil_id, permissao_id)
        SELECT perfis.id, permissoes.id
        FROM perfis, permissoes
        WHERE perfis.nome = 'GERENTE_CARTOES' AND permissoes.nome = permissao.nome
        ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
    END LOOP;

    -- Atribuir permissões ao perfil GERENTE_CLIENTES
    FOR permissao IN VALUES 
        ('ACESSO_SISTEMA',
         'VISUALIZAR_CLIENTES', 'CADASTRAR_CLIENTE', 'ALTERAR_CLIENTE', 'ANALISAR_CREDITO', 'LIBERAR_LIMITE_CLIENTE',
         'GERAR_RELATORIOS',
         'VISUALIZAR_CONTRATOS', 'CADASTRAR_CONTRATO', 'ALTERAR_CONTRATO',
         'VISUALIZAR_FATURAS')
    LOOP
        INSERT INTO perfil_permissoes (perfil_id, permissao_id)
        SELECT perfis.id, permissoes.id
        FROM perfis, permissoes
        WHERE perfis.nome = 'GERENTE_CLIENTES' AND permissoes.nome = permissao.nome
        ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
    END LOOP;

    -- Atribuir permissões ao perfil SUPERVISOR_ATENDIMENTO
    FOR permissao IN VALUES 
        ('ACESSO_SISTEMA',
         'VISUALIZAR_CARTOES', 'BLOQUEAR_CARTAO',
         'VISUALIZAR_CLIENTES', 'ALTERAR_CLIENTE',
         'VISUALIZAR_CONTRATOS',
         'VISUALIZAR_FATURAS')
    LOOP
        INSERT INTO perfil_permissoes (perfil_id, permissao_id)
        SELECT perfis.id, permissoes.id
        FROM perfis, permissoes
        WHERE perfis.nome = 'SUPERVISOR_ATENDIMENTO' AND permissoes.nome = permissao.nome
        ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
    END LOOP;

    -- Atribuir permissões ao perfil ANALISTA_RISCO
    FOR permissao IN VALUES 
        ('ACESSO_SISTEMA',
         'VISUALIZAR_CLIENTES', 'ANALISAR_CREDITO', 'LIBERAR_LIMITE_CLIENTE',
         'GERAR_RELATORIOS', 'VISUALIZAR_RELATORIOS_SENSIVEIS',
         'VISUALIZAR_CONTRATOS',
         'VISUALIZAR_FATURAS')
    LOOP
        INSERT INTO perfil_permissoes (perfil_id, permissao_id)
        SELECT perfis.id, permissoes.id
        FROM perfis, permissoes
        WHERE perfis.nome = 'ANALISTA_RISCO' AND permissoes.nome = permissao.nome
        ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
    END LOOP;

    -- Atribuir permissões ao perfil ATENDENTE_CARTOES
    FOR permissao IN VALUES 
        ('ACESSO_SISTEMA',
         'VISUALIZAR_CARTOES', 'BLOQUEAR_CARTAO',
         'VISUALIZAR_CLIENTES', 'ALTERAR_CLIENTE',
         'VISUALIZAR_CONTRATOS',
         'VISUALIZAR_FATURAS')
    LOOP
        INSERT INTO perfil_permissoes (perfil_id, permissao_id)
        SELECT perfis.id, permissoes.id
        FROM perfis, permissoes
        WHERE perfis.nome = 'ATENDENTE_CARTOES' AND permissoes.nome = permissao.nome
        ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
    END LOOP;

    -- Atribuir permissões ao perfil OPERADOR_CARTOES
    FOR permissao IN VALUES 
        ('ACESSO_SISTEMA',
         'VISUALIZAR_CARTOES',
         'VISUALIZAR_CLIENTES',
         'VISUALIZAR_CONTRATOS',
         'VISUALIZAR_FATURAS')
    LOOP
        INSERT INTO perfil_permissoes (perfil_id, permissao_id)
        SELECT perfis.id, permissoes.id
        FROM perfis, permissoes
        WHERE perfis.nome = 'OPERADOR_CARTOES' AND permissoes.nome = permissao.nome
        ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
    END LOOP;

    -- Atribuir permissões ao perfil CLIENTE_PF
    FOR permissao IN VALUES 
        ('ACESSO_SISTEMA',
         'VISUALIZAR_CARTOES',
         'VISUALIZAR_CLIENTES',
         'VISUALIZAR_CONTRATOS',
         'VISUALIZAR_FATURAS')
    LOOP
        INSERT INTO perfil_permissoes (perfil_id, permissao_id)
        SELECT perfis.id, permissoes.id
        FROM perfis, permissoes
        WHERE perfis.nome = 'CLIENTE_PF' AND permissoes.nome = permissao.nome
        ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
    END LOOP;

    -- Atribuir permissões ao perfil CLIENTE_PJ
    FOR permissao IN VALUES 
        ('ACESSO_SISTEMA',
         'VISUALIZAR_CARTOES',
         'VISUALIZAR_CLIENTES',
         'VISUALIZAR_CONTRATOS',
         'VISUALIZAR_FATURAS')
    LOOP
        INSERT INTO perfil_permissoes (perfil_id, permissao_id)
        SELECT perfis.id, permissoes.id
        FROM perfis, permissoes
        WHERE perfis.nome = 'CLIENTE_PJ' AND permissoes.nome = permissao.nome
        ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
    END LOOP;

    -- Atribuir permissões ao perfil AUDITOR
    FOR permissao IN VALUES 
        ('ACESSO_SISTEMA', 'AUDITAR_SISTEMA',
         'VISUALIZAR_CARTOES',
         'VISUALIZAR_CLIENTES', 'ANALISAR_CREDITO',
         'GERAR_RELATORIOS', 'EXPORTAR_DADOS', 'VISUALIZAR_RELATORIOS_SENSIVEIS',
         'VISUALIZAR_CONTRATOS',
         'VISUALIZAR_FATURAS')
    LOOP
        INSERT INTO perfil_permissoes (perfil_id, permissao_id)
        SELECT perfis.id, permissoes.id
        FROM perfis, permissoes
        WHERE perfis.nome = 'AUDITOR' AND permissoes.nome = permissao.nome
        ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
    END LOOP;
END $$;

-- Criar usuário administrador padrão se não existir
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'admin') THEN
        -- Primeiro, obter o ID da pessoa para o administrador
        INSERT INTO pessoas (tipo_pessoa, status, data_cadastro, data_atualizacao, 
                           email_principal, telefone_principal, celular_principal,
                           observacoes, cep, logradouro, numero, complemento, bairro, cidade, uf)
        VALUES ('USUARIO', 'ATIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
               'admin@empresa.com', '(00) 0000-0000', '(00) 00000-0000',
               'Usuário administrador padrão do sistema', '00000000', 'Rua do Admin', '1', 'Sala 1', 'Centro', 'Cidade', 'UF')
        RETURNING id INTO pessoa_id;

        -- Criar o usuário
        INSERT INTO usuarios (pessoa_id, username, senha, data_cadastro, data_atualizacao)
        VALUES (pessoa_id, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVYITi', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

        -- Associar perfil ADMIN ao usuário
        INSERT INTO usuario_perfis (usuario_id, perfil_id, data_atribuicao)
        SELECT u.pessoa_id, p.id, CURRENT_TIMESTAMP
        FROM usuarios u, perfis p
        WHERE u.username = 'admin' AND p.nome = 'ADMIN';
    END IF;
END $$;

-- Configurações finais
SET timezone = 'America/Sao_Paulo';

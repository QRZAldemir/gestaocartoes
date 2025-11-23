#!/bin/bash

# ==============================================
# Script para configurar PostgreSQL no Proxmox
# Sistema de Gestão de Cartões
# ==============================================

echo "🚀 Configurando PostgreSQL para Sistema de Gestão de Cartões"
echo "=============================================================="

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

# Verificar se o Docker está instalado
if ! command -v docker &> /dev/null; then
    error "Docker não está instalado. Instalando..."
    sudo apt update
    sudo apt install -y docker.io docker-compose
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -aG docker $USER
    log "Docker instalado com sucesso!"
fi

# Verificar se o PostgreSQL já está rodando
if docker ps | grep -q postgres; then
    warning "PostgreSQL já está rodando. Parando container existente..."
    docker stop postgres-gestao-cartoes
    docker rm postgres-gestao-cartoes
fi

# Criar diretório para dados do PostgreSQL
mkdir -p ~/postgres-data/gestao-cartoes

# Criar arquivo docker-compose.yml
cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: postgres-gestao-cartoes
    environment:
      POSTGRES_DB: gestao_cartoes
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres123
      POSTGRES_INITDB_ARGS: "--encoding=UTF-8 --lc-collate=pt_BR.UTF-8 --lc-ctype=pt_BR.UTF-8"
    ports:
      - "5432:5432"
    volumes:
      - ./postgres-data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d gestao_cartoes"]
      interval: 30s
      timeout: 10s
      retries: 3
    networks:
      - gestao-cartoes-network

  pgadmin:
    image: dpage/pgadmin4:latest
    container_name: pgadmin-gestao-cartoes
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@gestao-cartoes.com
      PGADMIN_DEFAULT_PASSWORD: admin123
    ports:
      - "8081:80"
    depends_on:
      - postgres
    restart: unless-stopped
    networks:
      - gestao-cartoes-network

networks:
  gestao-cartoes-network:
    driver: bridge
EOF

# Criar diretório para scripts de inicialização
mkdir -p init-scripts

# Criar script de inicialização do banco
cat > init-scripts/01-init-database.sql << 'EOF'
-- ==============================================
-- Script de inicialização do banco de dados
-- Sistema de Gestão de Cartões
-- ==============================================

-- Criar extensões necessárias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Configurar timezone
SET timezone = 'America/Sao_Paulo';

-- Criar usuário específico para a aplicação (opcional)
-- CREATE USER gestao_cartoes_app WITH PASSWORD 'app_password_123';
-- GRANT ALL PRIVILEGES ON DATABASE gestao_cartoes TO gestao_cartoes_app;

-- Log de inicialização
DO $$
BEGIN
    RAISE NOTICE 'Banco de dados gestao_cartoes inicializado com sucesso!';
    RAISE NOTICE 'Timezone configurado para: %', current_setting('timezone');
    RAISE NOTICE 'Extensões instaladas: uuid-ossp, pg_trgm';
END $$;
EOF

# Criar script de verificação
cat > init-scripts/02-check-database.sql << 'EOF'
-- Verificar se o banco foi criado corretamente
SELECT 
    datname as "Database",
    datcollate as "Collate",
    datctype as "Ctype",
    datconnlimit as "Connection Limit"
FROM pg_database 
WHERE datname = 'gestao_cartoes';

-- Verificar extensões
SELECT extname, extversion 
FROM pg_extension 
WHERE extname IN ('uuid-ossp', 'pg_trgm');
EOF

log "Iniciando containers PostgreSQL e pgAdmin..."

# Iniciar os containers
docker-compose up -d

# Aguardar o PostgreSQL estar pronto
log "Aguardando PostgreSQL estar pronto..."
sleep 10

# Verificar se o container está rodando
if docker ps | grep -q postgres-gestao-cartoes; then
    log "✅ PostgreSQL iniciado com sucesso!"
    
    # Testar conexão
    log "Testando conexão com o banco..."
    if docker exec postgres-gestao-cartoes pg_isready -U postgres -d gestao_cartoes; then
        log "✅ Conexão com PostgreSQL estabelecida!"
        
        # Executar scripts de verificação
        docker exec postgres-gestao-cartoes psql -U postgres -d gestao_cartoes -f /docker-entrypoint-initdb.d/02-check-database.sql
        
        echo ""
        echo "🎉 CONFIGURAÇÃO CONCLUÍDA COM SUCESSO!"
        echo "======================================"
        echo ""
        echo "📊 Informações de Conexão:"
        echo "   Host: localhost"
        echo "   Porta: 5432"
        echo "   Banco: gestao_cartoes"
        echo "   Usuário: postgres"
        echo "   Senha: postgres123"
        echo ""
        echo "🌐 pgAdmin (Interface Web):"
        echo "   URL: http://localhost:8081"
        echo "   Email: admin@gestao-cartoes.com"
        echo "   Senha: admin123"
        echo ""
        echo "🔧 Comandos úteis:"
        echo "   Parar containers: docker-compose down"
        echo "   Ver logs: docker-compose logs -f"
        echo "   Acessar PostgreSQL: docker exec -it postgres-gestao-cartoes psql -U postgres -d gestao_cartoes"
        echo ""
        echo "🚀 Agora você pode iniciar a aplicação Spring Boot!"
        
    else
        error "❌ Falha na conexão com PostgreSQL"
        docker-compose logs postgres
        exit 1
    fi
else
    error "❌ Falha ao iniciar PostgreSQL"
    docker-compose logs
    exit 1
fi





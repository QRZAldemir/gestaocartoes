#!/bin/bash

# ==============================================
# Script completo para configurar e implantar a aplicação
# Sistema de Gestão de Cartões
# ==============================================

echo "🚀 Configuração completa - Gestão de Cartões - Spring Boot"
echo "==========================================================="

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

# Verificar se o script está sendo executado como root
if [[ $EUID -ne 0 ]]; then
   error "Este script deve ser executado como root (use sudo)"
   exit 1
fi

# Diretórios
PROJECT_DIR="/home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes"
DEPLOY_DIR="/opt/gestao-cartoes"
JAR_NAME="gestao-cartoes.jar"

# Navegar para o diretório do projeto
cd $PROJECT_DIR
log "Navegando para o diretório do projeto: $PROJECT_DIR"

# Instalar dependências necessárias
log "Verificando e instalando dependências..."
apt update
apt install -y maven openjdk-17-jdk nginx postgresql

# Corrigir problema do diretório target
if [ -d "target" ]; then
    log "Removendo diretório target existente..."
    rm -rf target
fi

# Compilar a aplicação
log "Compilando a aplicação..."
mvn clean package -DskipTests -q

# Verificar se o build foi bem-sucedido
if [ $? -eq 0 ]; then
    log "✅ Build bem-sucedido!"

    # Verificar se o JAR foi gerado
    if [ -f "target/$JAR_NAME" ]; then
        log "Arquivo JAR encontrado: target/$JAR_NAME"
    else
        error "Arquivo JAR não encontrado após o build."
        exit 1
    fi
else
    error "❌ Falha no build da aplicação."
    exit 1
fi

# Criar diretório de implantação se não existir
if [ ! -d "$DEPLOY_DIR" ]; then
    log "Criando diretório de implantação: $DEPLOY_DIR"
    mkdir -p $DEPLOY_DIR
    chown -R www-data:www-data $DEPLOY_DIR
    chmod 755 $DEPLOY_DIR
fi

# Parar o serviço atual
log "Parando o serviço gestao-cartoes..."
systemctl stop gestao-cartoes 2>/dev/null

# Copiar o JAR para o diretório de implantação
log "Copiando JAR para o diretório de implantação..."
cp "target/$JAR_NAME" "$DEPLOY_DIR/"
chown www-data:www-data "$DEPLOY_DIR/$JAR_NAME"
chmod 755 "$DEPLOY_DIR/$JAR_NAME"

# Criar script de inicialização se não existir
if [ ! -f "$DEPLOY_DIR/start-app.sh" ]; then
    log "Criando script de inicialização..."
    cat > "$DEPLOY_DIR/start-app.sh" << 'EOF'
#!/bin/bash

# Script para iniciar a aplicação
cd /opt/gestao-cartoes
exec java -jar gestao-cartoes.jar --spring.profiles.active=prod
EOF
    chmod +x "$DEPLOY_DIR/start-app.sh"
    chown www-data:www-data "$DEPLOY_DIR/start-app.sh"
fi

# Criar serviço systemd
log "Criando serviço systemd..."
cat > /etc/systemd/system/gestao-cartoes.service << EOF
[Unit]
Description=Gestao Cartoes Spring Boot Application
After=network.target postgresql.service

[Service]
User=www-data
Group=www-data
WorkingDirectory=/opt/gestao-cartoes
ExecStart=/usr/bin/java -jar /opt/gestao-cartoes/gestao-cartoes.jar --spring.profiles.active=prod
ExecStop=/bin/kill -15 \$MAINPID
SuccessExitStatus=143
Restart=always
RestartSec=10

# Configurações de ambiente
Environment="JAVA_OPTS=-Xms512m -Xmx1024m"
Environment="SPRING_PROFILES_ACTIVE=prod"

# Limites de recursos
LimitNOFILE=65536
LimitMEMLOCK=infinity

[Install]
WantedBy=multi-user.target
EOF

# Recarregar o systemd
systemctl daemon-reload

# Configurar Nginx
log "Configurando Nginx..."
cat > /etc/nginx/sites-available/gestao-cartoes << 'EOF'
server {
    listen 80;
    server_name _;  # Aceitar qualquer nome de host

    # Diretório para logs
    access_log /var/log/nginx/gestao-cartoes/access.log;
    error_log /var/log/nginx/gestao-cartoes/error.log;

    # Diretório para arquivos estáticos
    root /opt/gestao-cartoes/static;
    index index.html;

    # Proxy para a aplicação Spring Boot
    location /gestao-cartoes {
        proxy_pass http://localhost:8080/gestao-cartoes;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Configurações de timeout
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;

        # Buffering
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 4k;

        # Configurações de cabeçalho para CORS
        add_header 'Access-Control-Allow-Origin' '*' always;
        add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS' always;
        add_header 'Access-Control-Allow-Headers' 'DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization' always;
    }

    # Configuração para a documentação da API (Swagger)
    location /gestao-cartoes/swagger-ui {
        proxy_pass http://localhost:8080/gestao-cartoes/swagger-ui;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Configuração para o health check
    location /gestao-cartoes/actuator {
        proxy_pass http://localhost:8080/gestao-cartoes/actuator;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Configuração para arquivos estáticos
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        root /opt/gestao-cartoes/static;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
EOF

# Habilitar o site do Nginx
ln -sf /etc/nginx/sites-available/gestao-cartoes /etc/nginx/sites-enabled/

# Remover o site padrão do Nginx
if [ -f /etc/nginx/sites-enabled/default ]; then
    rm /etc/nginx/sites-enabled/default
    log "Site padrão do Nginx removido."
fi

# Testar configuração do Nginx
log "Testando configuração do Nginx..."
nginx -t
if [ $? -eq 0 ]; then
    log "Configuração do Nginx válida. Reiniciando serviço..."
    systemctl reload nginx
else
    error "Erro na configuração do Nginx. Verificando os arquivos de configuração."
    exit 1
fi

# Habilitar e iniciar o serviço
log "Habilitando e iniciando o serviço..."
systemctl enable gestao-cartoes
systemctl start gestao-cartoes

# Verificar status do serviço
sleep 5
if systemctl is-active --quiet gestao-cartoes; then
    log "✅ Serviço gestao-cartoes está ativo!"

    # Verificar se a aplicação está respondendo
    log "Verificando se a aplicação está respondendo..."
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/gestao-cartoes/actuator/health | grep -q "200"; then
        log "✅ Aplicação está saudável!"
        info "A aplicação está disponível em: http://$(hostname)/gestao-cartoes"
        info "Swagger UI: http://$(hostname)/gestao-cartoes/swagger-ui.html"
    else
        warning "A aplicação pode não estar respondendo corretamente. Verificando logs..."
        journalctl -u gestao-cartoes --no-pager -n 20
    fi
else
    error "❌ Falha ao iniciar o serviço gestao-cartoes. Verificando logs..."
    journalctl -u gestao-cartoes --no-pager -n 20
fi

log "✅ Configuração completa concluída!"

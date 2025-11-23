#!/bin/bash

# ==============================================
# Script para corrigir problemas de implantação
# Sistema de Gestão de Cartões
# ==============================================

echo "🔧 Corrigindo problemas de implantação - Gestão de Cartões"
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

# Criar diretório de implantação se não existir
if [ ! -d "$DEPLOY_DIR" ]; then
    log "Criando diretório de implantação: $DEPLOY_DIR"
    mkdir -p $DEPLOY_DIR
    chown -R www-data:www-data $DEPLOY_DIR
    chmod 755 $DEPLOY_DIR
fi

# Parar o serviço atual
log "Parando o serviço gestao-cartoes..."
systemctl stop gestao-cartoes

# Verificar se o JAR existe no projeto
if [ -f "$PROJECT_DIR/target/$JAR_NAME" ]; then
    log "Arquivo JAR encontrado no projeto. Copiando para o diretório de implantação..."
    cp "$PROJECT_DIR/target/$JAR_NAME" "$DEPLOY_DIR/"
    chown www-data:www-data "$DEPLOY_DIR/$JAR_NAME"
    chmod 755 "$DEPLOY_DIR/$JAR_NAME"
    log "✅ Arquivo JAR copiado com sucesso!"
else
    warning "Arquivo JAR não encontrado no diretório target do projeto."

    # Tentar encontrar o JAR em outros locais
    JAR_LOCATION=$(find $PROJECT_DIR -name "$JAR_NAME" -type f 2>/dev/null | head -n 1)

    if [ -n "$JAR_LOCATION" ]; then
        log "Arquivo JAR encontrado em: $JAR_LOCATION"
        cp "$JAR_LOCATION" "$DEPLOY_DIR/"
        chown www-data:www-data "$DEPLOY_DIR/$JAR_NAME"
        chmod 755 "$DEPLOY_DIR/$JAR_NAME"
        log "✅ Arquivo JAR copiado com sucesso!"
    else
        error "Arquivo JAR não encontrado. É necessário compilar a aplicação primeiro."
        error "Execute: cd $PROJECT_DIR && mvn clean package"
        exit 1
    fi
fi

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

# Atualizar o serviço systemd se necessário
log "Verificando configuração do serviço systemd..."
if [ -f "/etc/systemd/system/gestao-cartoes.service" ]; then
    # Verificar se o caminho está correto
    if ! grep -q "ExecStart=/usr/bin/java -jar /opt/gestao-cartoes/gestao-cartoes.jar" /etc/systemd/system/gestao-cartoes.service; then
        log "Atualizando configuração do serviço systemd..."
        sed -i 's|ExecStart=/usr/bin/java -jar .*|ExecStart=/usr/bin/java -jar /opt/gestao-cartoes/gestao-cartoes.jar --spring.profiles.active=prod|' /etc/systemd/system/gestao-cartoes.service
        systemctl daemon-reload
        log "✅ Configuração do serviço atualizada!"
    fi
else
    warning "Arquivo de serviço systemd não encontrado. Criando um novo..."
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
    systemctl daemon-reload
    log "✅ Serviço systemd criado!"
fi

# Habilitar e iniciar o serviço
log "Habilitando e iniciando o serviço..."
systemctl enable gestao-cartoes
systemctl start gestao-cartoes

# Verificar status do serviço
sleep 3
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

log "✅ Correção de implantação concluída!"

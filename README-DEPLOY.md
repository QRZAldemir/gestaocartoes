# Configuração de Nginx para Aplicação Spring Boot

Este guia explica como configurar o Nginx para servir sua aplicação Spring Boot "Gestão de Cartões".

## Alterações Recentes no Código

### Correção de Erros de Compilação

Foram realizadas correções importantes na base de código para resolver erros de compilação:

1. **Classe Usuarios**:
   - Adicionados campos faltantes: `tentativasLogin`, `bloqueado` e `ultimoLogin`
   - Implementados métodos faltantes:
     - `setTentativasLogin(Integer tentativasLogin)`
     - `setBloqueado(Boolean bloqueado)`
     - `registrarLogin()`
     - `possuiPerfil(String nomePerfil)`
     - `podeRealizarOperacoes()`
   - Adicionado construtor para criação de usuários com username, email e senha

2. **Correção de Referências ao Enum StatusPessoa**:
   - O enum `StatusPessoa` está definido na classe `Pessoas`, não em `Usuarios`
   - Atualizadas todas as referências para usar `Pessoas.StatusPessoa` em vez de `Usuarios.StatusPessoa`
   - Arquivos afetados:
     - `UsuarioRepository.java`
     - `UsuarioController.java`
     - `UsuarioService.java`

3. **Correções nos Métodos de Usuário**:
   - Corrigida chamada ao método `removerPerfil` na classe `UsuarioService`
   - Corrigida criação de usuário padrão no método `criarUsuariosPadrao`

## Pré-requisitos

- Servidor Linux (Ubuntu/Debian recomendado)
- Java 17 instalado
- Maven instalado
- PostgreSQL instalado e configurado

## Configuração Inicial

### 1. Execute o script de configuração

```bash
# Dar permissão de execução ao script
chmod +x deploy.sh

# Executar como root
sudo ./deploy.sh
```

Este script irá:
- Instalar o Nginx (se não estiver instalado)
- Criar um serviço systemd para sua aplicação
- Configurar o Nginx para proxy requests para sua aplicação Spring Boot
- Criar scripts de implantação e monitoramento

### 2. Compile sua aplicação

```bash
# Compilar a aplicação
mvn clean package

# O arquivo JAR será gerado em target/gestao-cartoes.jar
```

### 3. Implantar a aplicação

```bash
# Copiar o JAR para o diretório de implantação
sudo cp target/gestao-cartoes.jar /opt/gestao-cartoes/

# Executar o script de implantação
sudo /opt/gestao-cartoes/deploy-app.sh
```

## Estrutura de Diretórios Criados

```
/opt/gestao-cartoes/
├── gestao-cartoes.jar         # Aplicação principal
├── deploy-app.sh             # Script de implantação
├── monitor.sh                # Script de monitoramento
├── static/                   # Diretório para arquivos estáticos
└── backups/                  # Diretório para backups da aplicação

/etc/systemd/system/gestao-cartoes.service  # Serviço systemd
/etc/nginx/sites-available/gestao-cartoes   # Configuração do Nginx
/etc/nginx/sites-enabled/gestao-cartoes    # Link para a configuração habilitada
```

## Endpoints Disponíveis

Após a configuração, sua aplicação estará disponível nos seguintes endpoints:

- **Aplicação principal**: http://seu-servidor/gestao-cartoes
- **Documentação da API (Swagger UI)**: http://seu-servidor/gestao-cartoes/swagger-ui.html
- **Health Check**: http://seu-servidor/gestao-cartoes/actuator/health

## Gerenciamento da Aplicação

### Iniciar/Parar/Reiniciar a aplicação

```bash
# Iniciar a aplicação
sudo systemctl start gestao-cartoes

# Parar a aplicação
sudo systemctl stop gestao-cartoes

# Reiniciar a aplicação
sudo systemctl restart gestao-cartoes

# Verificar status
sudo systemctl status gestao-cartoes

# Habilitar para iniciar com o sistema
sudo systemctl enable gestao-cartoes
```

### Monitoramento

```bash
# Executar manualmente o script de monitoramento
sudo /opt/gestao-cartoes/monitor.sh

# Verificar logs da aplicação
sudo journalctl -u gestao-cartoes -f

# Verificar logs do Nginx
sudo tail -f /var/log/nginx/gestao-cartoes/access.log
sudo tail -f /var/log/nginx/gestao-cartoes/error.log
```

### Atualização da aplicação

Para atualizar sua aplicação:

1. Compile a nova versão:
   ```bash
   mvn clean package
   ```

2. Implante a nova versão:
   ```bash
   sudo /opt/gestao-cartoes/deploy-app.sh
   ```

O script fará backup da versão atual e implantará a nova versão automaticamente.

## Configuração SSL (Opcional)

Para habilitar HTTPS:

1. Obtenha um certificado SSL (usando Let's Encrypt, por exemplo):
   ```bash
   sudo apt install certbot python3-certbot-nginx
   sudo certbot --nginx -d seu-dominio.com
   ```

2. Descomente as linhas SSL na configuração do Nginx:
   ```bash
   sudo nano /etc/nginx/sites-available/gestao-cartoes
   ```

3. Remente as linhas que começam com `#` no bloco de configuração SSL.

## Personalização da Configuração

### Alterar porta da aplicação

Se precisar alterar a porta da sua aplicação Spring Boot:

1. Edite o arquivo de configuração:
   ```bash
   sudo nano /etc/nginx/sites-available/gestao-cartoes
   ```

2. Altere a linha `proxy_pass http://localhost:8080/gestao-cartoes;` para a nova porta.

3. Reinicie o Nginx:
   ```bash
   sudo systemctl reload nginx
   ```

### Adicionar mais endpoints para proxy

Se precisar adicionar mais endpoints para serem proxied para sua aplicação:

1. Edite o arquivo de configuração:
   ```bash
   sudo nano /etc/nginx/sites-available/gestao-cartoes
   ```

2. Adicione novos blocos `location` conforme necessário.

3. Reinicie o Nginx:
   ```bash
   sudo systemctl reload nginx
   ```

## Solução de Problemas

### A aplicação não está acessível

1. Verifique se a aplicação está rodando:
   ```bash
   sudo systemctl status gestao-cartoes
   ```

2. Verifique os logs da aplicação:
   ```bash
   sudo journalctl -u gestao-cartoes -n 50
   ```

3. Verifique os logs do Nginx:
   ```bash
   sudo tail -n 50 /var/log/nginx/gestao-cartoes/error.log
   ```

4. Teste a configuração do Nginx:
   ```bash
   sudo nginx -t
   ```

### Performance issues

1. Verifique o uso de recursos:
   ```bash
   htop
   ```

2. Verifique os logs de erros do Nginx:
   ```bash
   sudo tail -f /var/log/nginx/gestao-cartoes/error.log
   ```

3. Ajuste os parâmetros de timeout e buffer na configuração do Nginx conforme necessário.
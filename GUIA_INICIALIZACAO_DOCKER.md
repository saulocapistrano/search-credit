# Guia de Inicialização do Ambiente Docker

## 📋 Visão Geral do Ecossistema

Este ecossistema é composto por três projetos principais:

1. **search-credit** (Projeto Principal)
   - API REST em Spring Boot
   - PostgreSQL 15
   - Apache Kafka + Zookeeper
   - **Funciona isoladamente**

2. **credito-analise-worker** (Opcional)
   - Worker que consome eventos Kafka
   - Depende do Kafka do `search-credit`

3. **search-credit-frontend** (Opcional)
   - Frontend Angular
   - Depende da API `search-credit`

---

## 🔗 Mapa de Dependências

```
┌─────────────────────────────────────────┐
│         search-credit                   │
│  ┌──────────┐  ┌──────────┐  ┌────────┐│
│  │ Postgres │  │ Zookeeper│  │ Kafka  ││
│  └──────────┘  └──────────┘  └────────┘│
│         │              │         │      │
│         └──────────────┴─────────┘      │
│                    │                    │
│              ┌─────▼─────┐              │
│              │   API     │              │
│              │ Spring    │              │
│              │   Boot    │              │
│              └─────┬─────┘              │
└────────────────────┼────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
   ┌────▼────┐  ┌────▼────┐  ┌────▼────┐
   │ Worker  │  │Frontend │  │ (outros)│
   │ Kafka   │  │ Angular │  │         │
   └─────────┘  └─────────┘  └─────────┘
```

### Dependências Detalhadas

| Serviço | Depende De | Tipo de Dependência |
|---------|------------|---------------------|
| `search-credit` API | PostgreSQL, Kafka, Zookeeper | **Obrigatória** - A API não funciona sem eles |
| `credito-analise-worker` | Kafka (do search-credit) | **Opcional** - Worker funciona apenas se Kafka estiver ativo |
| `search-credit-frontend` | API search-credit | **Opcional** - Frontend precisa da API rodando |

---

## ✅ Checklist de Pré-requisitos

Antes de iniciar, verifique:

- [ ] Docker instalado e funcionando
  ```bash
  docker --version
  docker compose version
  ```

- [ ] Portas disponíveis:
  - `5437` - PostgreSQL
  - `9094` - Kafka (externa)
  - `9092` - Kafka (interna)
  - `2182` - Zookeeper
  - `8189` - API search-credit

- [ ] Repositórios clonados:
  - [ ] `search-credit` (obrigatório)
  - [ ] `credito-analise-worker` (opcional)
  - [ ] `search-credit-frontend` (opcional)

---

## 🚀 Passo a Passo de Inicialização

### FASE 1: Infraestrutura Base (search-credit)

**Objetivo:** Subir PostgreSQL, Zookeeper e Kafka antes da API.

#### Passo 1.1: Navegar para o diretório do projeto principal

```bash
cd search-credit
```

#### Passo 1.2: Compilar a aplicação (necessário para o build Docker)

```bash
./mvnw clean package
```

**Por quê?** O Dockerfile copia o JAR compilado (`target/search-credit-0.0.1-SNAPSHOT.jar`).

#### Passo 1.3: Subir apenas a infraestrutura (sem a API)

```bash
docker compose up -d postgres zookeeper kafka
```

**Por quê essa ordem?**
- `zookeeper` deve iniciar antes do `kafka` (Kafka depende do Zookeeper)
- `postgres` pode iniciar em paralelo (não tem dependências)
- `kafka` aguarda o Zookeeper estar pronto

#### Passo 1.4: Verificar se os serviços estão rodando

```bash
docker compose ps
```

**Saída esperada:**
```
NAME                      STATUS
search-credit-postgres    Up
search-credit-zookeeper   Up
search-credit-kafka       Up
```

#### Passo 1.5: Aguardar Kafka estar totalmente pronto

```bash
docker compose logs kafka | grep "started (kafka.server.KafkaServer)"
```

**Aguarde até ver:** `started (kafka.server.KafkaServer)`

**Tempo estimado:** 10-30 segundos após o container iniciar.

**Por quê?** O Kafka precisa inicializar completamente antes de aceitar conexões. A API pode falhar se tentar conectar muito cedo.

#### Passo 1.6: Subir a API search-credit

```bash
docker compose up -d search-credit
```

**Por quê agora?** A API depende do PostgreSQL e Kafka estarem prontos e aceitando conexões.

#### Passo 1.7: Verificar logs da API

```bash
docker compose logs -f search-credit
```

**Aguarde até ver:** `Started SearchCreditApplication`

**Indicadores de sucesso:**
- ✅ `Started SearchCreditApplication`
- ✅ `Liquibase has been successfully executed`
- ✅ Sem erros de conexão com PostgreSQL ou Kafka

**Indicadores de erro:**
- ❌ `Connection refused` → Infraestrutura não está pronta
- ❌ `Liquibase checksum` → Problema com migrações
- ❌ `Kafka bootstrap servers` → Kafka não está acessível

#### Passo 1.8: Validar que a API está respondendo

```bash
curl http://localhost:8189/actuator/health
```

**Ou acesse no navegador:**
```
http://localhost:8189/swagger-ui.html
```

---

### FASE 2: Worker de Análise (Opcional)

**Objetivo:** Subir o worker que consome eventos Kafka.

**Pré-requisito:** FASE 1 completa e Kafka funcionando.

#### Passo 2.1: Navegar para o diretório do worker

```bash
cd ../credito-analise-worker
```

#### Passo 2.2: Verificar configuração do Kafka

O worker deve estar configurado para conectar no mesmo Kafka do `search-credit`:

- **Se estiver na mesma rede Docker:** `search-credit-kafka:9092`
- **Se estiver rodando localmente:** `localhost:9094`

#### Passo 2.3: Subir o worker

```bash
docker compose up -d
```

**Ou, se não tiver docker-compose próprio:**

```bash
./mvnw spring-boot:run
```

#### Passo 2.4: Verificar logs do worker

```bash
docker compose logs -f credito-analise-worker
```

**Aguarde até ver:** Worker conectado ao Kafka e consumindo tópico `consulta-creditos-topic`.

---

### FASE 3: Frontend (Opcional)

**Objetivo:** Subir o frontend Angular.

**Pré-requisito:** FASE 1 completa e API `search-credit` respondendo.

#### Passo 3.1: Navegar para o diretório do frontend

```bash
cd ../search-credit-frontend
```

#### Passo 3.2: Verificar configuração da API

O frontend deve estar configurado para apontar para:
```
http://localhost:8189
```

#### Passo 3.3: Subir o frontend

```bash
docker compose up -d
```

**Ou, se usar npm/ng:**

```bash
npm install
ng serve
```

#### Passo 3.4: Acessar o frontend

```
http://localhost:4200
```

---

## 🔍 Comandos de Verificação e Troubleshooting

### Verificar status de todos os containers

```bash
docker compose ps
```

### Ver logs de um serviço específico

```bash
docker compose logs -f <nome-do-servico>
```

**Exemplos:**
```bash
docker compose logs -f search-credit
docker compose logs -f kafka
docker compose logs -f postgres
```

### Verificar conectividade entre containers

```bash
# Testar conexão com PostgreSQL
docker compose exec search-credit ping search-credit-postgres

# Testar conexão com Kafka
docker compose exec search-credit ping search-credit-kafka
```

### Verificar se Kafka está aceitando conexões

```bash
docker compose exec kafka kafka-topics.sh --list --bootstrap-server localhost:9092
```

**Saída esperada:** Lista de tópicos, incluindo `consulta-creditos-topic` (se já foi criado).

### Verificar saúde do PostgreSQL

```bash
docker compose exec postgres pg_isready -U postgres
```

### Reiniciar um serviço específico

```bash
docker compose restart <nome-do-servico>
```

**Exemplo:**
```bash
docker compose restart search-credit
```

### Parar todos os serviços

```bash
docker compose down
```

**Importante:** Isso mantém os volumes. Para remover volumes também:

```bash
docker compose down -v
```

---

## ⚠️ Problemas Comuns e Soluções

### Problema 1: API não consegue conectar ao PostgreSQL

**Sintoma:**
```
Connection to search-credit-postgres:5432 refused
```

**Solução:**
1. Verificar se PostgreSQL está rodando: `docker compose ps postgres`
2. Verificar logs: `docker compose logs postgres`
3. Aguardar alguns segundos e tentar novamente

### Problema 2: API não consegue conectar ao Kafka

**Sintoma:**
```
Bootstrap broker search-credit-kafka:9092 disconnected
```

**Solução:**
1. Verificar se Kafka está rodando: `docker compose ps kafka`
2. Verificar se Zookeeper está rodando: `docker compose ps zookeeper`
3. Aguardar Kafka inicializar completamente (ver Passo 1.5)
4. Verificar logs: `docker compose logs kafka`

### Problema 3: Porta já em uso

**Sintoma:**
```
Bind for 0.0.0.0:8189 failed: port is already allocated
```

**Solução:**
1. Identificar o processo usando a porta:
   ```bash
   # Windows
   netstat -ano | findstr :8189
   
   # Linux/Mac
   lsof -i :8189
   ```
2. Parar o processo ou alterar a porta no `docker-compose.yml`

### Problema 4: Worker não recebe eventos

**Sintoma:** Worker está rodando mas não processa eventos.

**Solução:**
1. Verificar se o worker está conectado ao Kafka correto
2. Verificar se o tópico existe:
   ```bash
   docker compose exec kafka kafka-topics.sh --list --bootstrap-server localhost:9092
   ```
3. Verificar logs do worker para erros de conexão

---

## 📊 Ordem de Inicialização Resumida

```
1. Infraestrutura Base
   ├── Zookeeper (primeiro)
   ├── PostgreSQL (pode ser paralelo)
   └── Kafka (depois do Zookeeper)

2. API search-credit
   └── Aguarda infraestrutura estar pronta

3. Worker (opcional)
   └── Aguarda Kafka estar pronto

4. Frontend (opcional)
   └── Aguarda API estar respondendo
```

---

## 🛑 Ordem de Parada (Shutdown)

Para parar os serviços na ordem inversa:

```bash
# 1. Parar frontend (se estiver rodando)
cd search-credit-frontend
docker compose down

# 2. Parar worker (se estiver rodando)
cd ../credito-analise-worker
docker compose down

# 3. Parar API e infraestrutura
cd ../search-credit
docker compose down
```

**Ou parar tudo de uma vez:**

```bash
cd search-credit
docker compose down
```

---

## 📝 Notas Importantes

1. **Persistência de Dados:**
   - O volume `pgdata` mantém os dados do PostgreSQL mesmo após `docker compose down`
   - Para limpar completamente: `docker compose down -v`

2. **Rede Docker:**
   - Todos os serviços estão na mesma rede: `search-credit-network`
   - Serviços se comunicam pelo nome do container (ex: `search-credit-postgres`)

3. **Portas Expostas:**
   - As portas expostas são para acesso externo (host)
   - Internamente, os serviços usam as portas padrão (5432 para Postgres, 9092 para Kafka)

4. **Desenvolvimento Local vs Docker:**
   - Se rodar a API localmente (`./mvnw spring-boot:run`), use `localhost:5437` e `localhost:9094`
   - Se rodar no Docker, use os nomes dos serviços: `search-credit-postgres:5432` e `search-credit-kafka:9092`

---

## ✅ Checklist Final de Validação

Após seguir todos os passos, valide:

- [ ] PostgreSQL está respondendo na porta 5437
- [ ] Kafka está respondendo na porta 9094
- [ ] API search-credit está respondendo em http://localhost:8189
- [ ] Swagger está acessível em http://localhost:8189/swagger-ui.html
- [ ] Worker está consumindo eventos (se aplicável)
- [ ] Frontend está acessível (se aplicável)
- [ ] Logs não mostram erros de conexão

---

**Última atualização:** 2024-12-26


# Search Credit - API de Consulta e Análise de Créditos

## Visão Geral

Sistema de consulta e análise de créditos constituídos desenvolvido como desafio técnico para vaga backend Java. A API permite consultar créditos por número de NFSe ou por número de crédito, seguindo princípios de Clean Architecture e Domain-Driven Design (DDD).

Este projeto faz parte de um **ecossistema completo** que inclui:
- **search-credit** (este repositório) - API Backend principal
- **search-credit-frontend** - Frontend Angular para interface web
- **credito-analise-worker** - Worker Kafka para processamento assíncrono

### Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.3.5**
- **PostgreSQL 15** (banco de dados relacional)
- **Apache Kafka 3.6** (comunicação assíncrona)
- **Docker & Docker Compose** (orquestração de serviços)
- **Liquibase** (migrações de banco de dados)
- **JUnit 5** (testes unitários)
- **Mockito** (mocks para testes)
- **Testcontainers** (testes de integração)

## Arquitetura

O projeto segue os princípios de **Clean Architecture** e **Domain-Driven Design (DDD)**, organizando o código em camadas bem definidas:

- **Domain**: Entidades e interfaces de repositório (regras de negócio puras)
- **Application**: DTOs e serviços de aplicação (casos de uso)
- **Infrastructure**: Implementações concretas (JPA, Kafka)
- **Interfaces**: Controllers REST (camada de apresentação)

A comunicação assíncrona é realizada através do **Apache Kafka**, onde o `search-credit` atua como **producer**, publicando eventos de consulta de créditos no tópico `consulta-creditos-topic`.

### Ecossistema Completo

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
   ┌────▼────┐  ┌────▼────┐      │
   │ Worker  │  │Frontend │      │
   │ Kafka   │  │ Angular │      │
   └─────────┘  └─────────┘      │
```

## Pré-requisitos Obrigatórios

⚠️ **IMPORTANTE:** Antes de começar, certifique-se de que:

- [ ] **Docker Desktop está instalado e RODANDO**
  ```bash
  # Verificar se Docker está rodando
  docker ps
  ```
  Se o comando acima falhar, inicie o Docker Desktop e aguarde até que ele esteja totalmente inicializado.

- [ ] **Docker Compose está disponível**
  ```bash
  docker compose version
  ```

- [ ] **Portas disponíveis** (verifique se não estão em uso):
  - `5437` - PostgreSQL
  - `9094` - Kafka (acesso externo)
  - `9092` - Kafka (interno)
  - `2182` - Zookeeper
  - `8189` - API search-credit
  - `8081` - Worker (se executar o worker)
  - `4200` - Frontend (se executar o frontend)

- [ ] **Java 17+ e Maven 3.6+** (para compilação local, se necessário)

## Como Executar o Projeto Completo

### Opção 1: Executar Apenas a API (Recomendado para Avaliação)

A API funciona **totalmente de forma isolada** e é suficiente para avaliação do desafio técnico.

#### Passo 1: Clone o Repositório

```bash
git clone https://github.com/saulocapistrano/search-credit.git
cd search-credit
```

#### Passo 2: Criar a Rede Docker Compartilhada

```bash
docker network create search-credit-network
```

**Nota:** Se a rede já existir, você receberá uma mensagem informando isso. Isso é normal e pode ser ignorado.

#### Passo 3: Compilar o Projeto

```bash
./mvnw clean package
```

**Por quê?** O Dockerfile precisa do JAR compilado para construir a imagem.

#### Passo 4: Subir a Infraestrutura (PostgreSQL, Zookeeper, Kafka)

```bash
docker compose up -d postgres zookeeper kafka
```

**Ordem importante:**
- Zookeeper deve iniciar antes do Kafka
- PostgreSQL pode iniciar em paralelo
- Aguarde alguns segundos para os serviços iniciarem completamente

#### Passo 5: Verificar se os Serviços Estão Rodando

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

#### Passo 6: Aguardar Kafka Estar Totalmente Pronto

```bash
# Windows PowerShell
docker compose logs kafka | Select-String "started (kafka.server.KafkaServer)"

# Linux/Mac
docker compose logs kafka | grep "started (kafka.server.KafkaServer)"
```

**Aguarde até ver:** `started (kafka.server.KafkaServer)`

**Tempo estimado:** 10-30 segundos após o container iniciar.

**Por quê?** O Kafka precisa inicializar completamente antes de aceitar conexões. A API pode falhar se tentar conectar muito cedo.

#### Passo 7: Subir a API

```bash
docker compose up -d search-credit
```

#### Passo 8: Verificar Logs da API

```bash
docker compose logs -f search-credit
```

**Aguarde até ver:**
- ✅ `Started SearchCreditApplication`
- ✅ `Liquibase has been successfully executed`
- ✅ Sem erros de conexão

**Indicadores de sucesso:**
- ✅ API iniciada com sucesso
- ✅ Banco de dados conectado
- ✅ Kafka conectado
- ✅ Migrações executadas

**Indicadores de erro:**
- ❌ `Connection refused` → Infraestrutura não está pronta (aguarde mais tempo)
- ❌ `Liquibase checksum` → Problema com migrações (execute `docker compose down -v` e tente novamente)

#### Passo 9: Validar que a API Está Respondendo

```bash
# Testar endpoint de saúde (se disponível)
curl http://localhost:8189/actuator/health

# Ou testar um endpoint real
curl http://localhost:8189/api/creditos/7891011
```

**Ou acesse no navegador:**
- **Swagger UI:** http://localhost:8189/swagger-ui.html
- **API Base:** http://localhost:8189

### Opção 2: Executar Ecossistema Completo (Frontend + Worker)

Para executar o ecossistema completo com frontend e worker, siga os passos abaixo na ordem correta.

#### FASE 1: Backend (search-credit)

Siga os passos 1 a 9 da **Opção 1** acima.

#### FASE 2: Worker (credito-analise-worker)

1. **Clone o repositório do worker:**
   ```bash
   cd ..
   git clone https://github.com/saulocapistrano/credito-analise-worker.git
   cd credito-analise-worker
   ```

2. **Compilar o worker:**
   ```bash
   ./mvnw clean package
   ```

3. **Subir o worker:**
   ```bash
   docker compose up -d worker
   ```

4. **Verificar logs:**
   ```bash
   docker compose logs -f worker
   ```

5. **Aguardar até ver:** Worker conectado ao Kafka e consumindo tópico `consulta-creditos-topic`

#### FASE 3: Frontend (search-credit-frontend)

1. **Clone o repositório do frontend:**
   ```bash
   cd ..
   git clone https://github.com/saulocapistrano/search-credit-frontend.git
   cd search-credit-frontend
   ```

2. **Subir o frontend:**
   ```bash
   docker compose up -d --build
   ```

3. **Acessar o frontend:**
   ```
   http://localhost:4200
   ```

**Nota:** O frontend utiliza Nginx como proxy reverso, direcionando requisições `/api/` para o backend em `http://search-credit:8189`.

## Endpoints Disponíveis

### GET /api/creditos/{numeroNfse}

Consulta todos os créditos associados a um número de NFSe.

**Exemplo de requisição:**
```bash
GET http://localhost:8189/api/creditos/7891011
```

**Exemplo de resposta:**
```json
[
  {
    "numeroCredito": "123456",
    "numeroNfse": "7891011",
    "dataConstituicao": "2024-02-25",
    "valorIssqn": 1500.75,
    "tipoCredito": "ISSQN",
    "simplesNacional": "Sim",
    "aliquota": 5.0,
    "valorFaturado": 30000.00,
    "valorDeducao": 5000.00,
    "baseCalculo": 25000.00
  },
  {
    "numeroCredito": "789012",
    "numeroNfse": "7891011",
    "dataConstituicao": "2024-02-26",
    "valorIssqn": 1200.50,
    "tipoCredito": "ISSQN",
    "simplesNacional": "Não",
    "aliquota": 4.5,
    "valorFaturado": 25000.00,
    "valorDeducao": 4000.00,
    "baseCalculo": 21000.00
  }
]
```

### GET /api/creditos/credito/{numeroCredito}

Consulta um crédito específico pelo seu número.

**Exemplo de requisição:**
```bash
GET http://localhost:8189/api/creditos/credito/123456
```

**Exemplo de resposta:**
```json
{
  "numeroCredito": "123456",
  "numeroNfse": "7891011",
  "dataConstituicao": "2024-02-25",
  "valorIssqn": 1500.75,
  "tipoCredito": "ISSQN",
  "simplesNacional": "Sim",
  "aliquota": 5.0,
  "valorFaturado": 30000.00,
  "valorDeducao": 5000.00,
  "baseCalculo": 25000.00
}
```

**Status HTTP:**
- `200 OK` - Crédito encontrado
- `404 Not Found` - Crédito não encontrado

## Testes Automatizados

O projeto possui testes unitários utilizando JUnit 5 e Mockito.

### Executar os Testes

```bash
./mvnw clean test
```

**Cobertura de Testes:**
- ✅ Testes unitários do `CreditoService` (11 testes)
- ✅ Uso de Mockito para mockar dependências
- ✅ Padrão Arrange/Act/Assert
- ✅ Testes isolados sem dependências externas

### Executar Testes Específicos

```bash
./mvnw test -Dtest=CreditoServiceTest
```

## Projetos do Ecossistema

### 1. search-credit (Este Repositório)

**Repositório:** https://github.com/saulocapistrano/search-credit

**Responsabilidades:**
- API REST para consulta de créditos
- Gerenciamento de PostgreSQL
- Publicação de eventos Kafka
- **Funciona isoladamente** - não requer outros serviços

### 2. credito-analise-worker

**Repositório:** https://github.com/saulocapistrano/credito-analise-worker

**Responsabilidades:**
- Consumir eventos Kafka do tópico `consulta-creditos-topic`
- Processar eventos de consulta de forma assíncrona
- Simular análise automática de crédito
- Group ID: `analise-group`
- Porta: `8081`

**Dependências:**
- Requer Kafka do `search-credit` rodando
- Deve estar na mesma rede Docker (`search-credit-network`)

**Como Executar:**
```bash
git clone https://github.com/saulocapistrano/credito-analise-worker.git
cd credito-analise-worker
./mvnw clean package
docker compose up -d worker
```

### 3. search-credit-frontend

**Repositório:** https://github.com/saulocapistrano/search-credit-frontend

**Responsabilidades:**
- Interface web Angular para consulta de créditos
- Tela de consulta por NFS-e
- Tela de consulta por número do crédito
- Tabela responsiva de resultados
- Modal de detalhes do crédito
- Porta: `4200`

**Dependências:**
- Requer API `search-credit` rodando
- Utiliza Nginx como proxy reverso

**Como Executar:**
```bash
git clone https://github.com/saulocapistrano/search-credit-frontend.git
cd search-credit-frontend
docker compose up -d --build
```

**Acessar:** http://localhost:4200

## Comunicação Assíncrona (Kafka)

O `search-credit` atua como **producer** no ecossistema Kafka, publicando eventos no tópico `consulta-creditos-topic` sempre que consultas são realizadas.

**Fluxo de Eventos:**
1. Usuário consulta crédito via API
2. `search-credit` publica evento no Kafka
3. `credito-analise-worker` consome e processa o evento
4. Evento registrado para auditoria

**Configuração Kafka:**
- Bootstrap servers: `search-credit-kafka:9092` (rede Docker) ou `localhost:9094` (acesso externo)
- Tópico: `consulta-creditos-topic`
- Serialização: JSON (JsonSerializer)
- Consumer Group: `analise-group` (worker)

## Troubleshooting

### Problema: Docker Desktop não está rodando

**Sintoma:** `Cannot connect to the Docker daemon`

**Solução:**
1. Inicie o Docker Desktop
2. Aguarde até que o ícone do Docker fique verde/ativo
3. Verifique com: `docker ps`

### Problema: Porta já está em uso

**Sintoma:** `Bind for 0.0.0.0:8189 failed: port is already allocated`

**Solução:**
1. Identifique o processo usando a porta:
   ```bash
   # Windows
   netstat -ano | findstr :8189
   
   # Linux/Mac
   lsof -i :8189
   ```
2. Pare o processo ou altere a porta no `docker-compose.yml`

### Problema: API não consegue conectar ao PostgreSQL

**Sintoma:** `Connection to search-credit-postgres:5432 refused`

**Solução:**
1. Verifique se PostgreSQL está rodando: `docker compose ps postgres`
2. Verifique logs: `docker compose logs postgres`
3. Aguarde alguns segundos e tente novamente

### Problema: API não consegue conectar ao Kafka

**Sintoma:** `Bootstrap broker search-credit-kafka:9092 disconnected`

**Solução:**
1. Verifique se Kafka está rodando: `docker compose ps kafka`
2. Verifique se Zookeeper está rodando: `docker compose ps zookeeper`
3. Aguarde Kafka inicializar completamente (ver Passo 6)
4. Verifique logs: `docker compose logs kafka`

### Problema: Rede Docker não existe

**Sintoma:** `network search-credit-network not found`

**Solução:**
```bash
docker network create search-credit-network
```

### Problema: Worker não recebe eventos

**Solução:**
1. Verifique se o worker está na mesma rede: `docker network inspect search-credit-network`
2. Verifique se o tópico existe:
   ```bash
   docker exec -it search-credit-kafka kafka-topics.sh --list --bootstrap-server localhost:9092
   ```
3. Verifique logs do worker: `docker compose logs worker`

## Comandos Úteis

### Verificar Status de Todos os Serviços

```bash
docker compose ps
```

### Ver Logs de um Serviço Específico

```bash
docker compose logs -f search-credit
docker compose logs -f kafka
docker compose logs -f postgres
```

### Parar Todos os Serviços

```bash
docker compose down
```

### Parar e Remover Volumes (Limpar Banco de Dados)

```bash
docker compose down -v
```

**⚠️ Atenção:** Isso apagará todos os dados do PostgreSQL!

### Reiniciar um Serviço Específico

```bash
docker compose restart search-credit
```

### Rebuild da Aplicação

```bash
./mvnw clean package
docker compose up -d --build search-credit
```

## Estrutura do Projeto

```
search-credit/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/searchcredit/
│   │   │       ├── application/          # Camada de aplicação
│   │   │       │   ├── dto/              # DTOs de resposta
│   │   │       │   └── service/          # Serviços de aplicação
│   │   │       ├── domain/               # Camada de domínio
│   │   │       │   ├── entity/           # Entidades de domínio
│   │   │       │   └── repository/       # Interfaces de repositório
│   │   │       ├── infrastructure/       # Camada de infraestrutura
│   │   │       │   ├── kafka/           # Configuração e publisher Kafka
│   │   │       │   └── repository/      # Implementações JPA
│   │   │       ├── interfaces/          # Camada de interface
│   │   │       │   └── controller/      # Controllers REST
│   │   │       └── SearchCreditApplication.java
│   │   └── resources/
│   │       ├── application.yml          # Configurações da aplicação
│   │       └── db/
│   │           └── changelog/           # Migrações Liquibase
│   └── test/
│       └── java/
│           └── br/com/searchcredit/
│               └── application/
│                   └── service/          # Testes unitários
├── docker-compose.yml                   # Orquestração Docker
├── Dockerfile                           # Imagem Docker da API
└── pom.xml                              # Dependências Maven
```

## Padrões de Projeto Utilizados

- **MVC (Model-View-Controller)**: Separação entre Controller, Service e Repository
- **Repository Pattern**: Abstração de acesso a dados
- **DTO Pattern**: Transferência de dados entre camadas
- **Builder Pattern**: Construção de objetos complexos (Lombok)
- **Dependency Injection**: Injeção de dependências via Spring
- **Clean Architecture**: Separação em camadas (Domain, Application, Infrastructure, Interfaces)
- **DDD (Domain-Driven Design)**: Entidades e repositórios no domínio

## Observações Finais

Este desafio técnico demonstra:

- ✅ **Arquitetura limpa** e organização de código
- ✅ **Padrões de design** e boas práticas (SOLID, DRY, KISS)
- ✅ **Integração com banco de dados** relacional (PostgreSQL)
- ✅ **Comunicação assíncrona** via mensageria (Kafka)
- ✅ **Testes automatizados** (JUnit 5, Mockito)
- ✅ **Containerização** completa (Docker)
- ✅ **Documentação** profissional e completa

### Projeto Principal vs Ecossistema

- **Para avaliação do desafio técnico:** Apenas este repositório (`search-credit`) é suficiente
- **Para demonstração arquitetural completa:** Execute o ecossistema completo (frontend + worker)

O projeto principal funciona **totalmente de forma isolada** e não requer outros serviços para funcionar.

---

## Links dos Repositórios

- **Backend (Principal):** https://github.com/saulocapistrano/search-credit
- **Frontend:** https://github.com/saulocapistrano/search-credit-frontend
- **Worker:** https://github.com/saulocapistrano/credito-analise-worker

---

**Desenvolvido para avaliação técnica** 🚀

**Última atualização:** 2024-12-28

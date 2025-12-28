# Search Credit - API de Consulta de Créditos

API REST desenvolvida em Spring Boot para consulta de créditos constituídos. Desenvolvida como desafio técnico seguindo princípios de Clean Architecture e Domain-Driven Design (DDD).

## Execução Rápida

### Pré-requisitos Obrigatórios

- **Docker Desktop** instalado e **rodando**
- **Java 17+** (para compilação local, se necessário)
- **Maven 3.6+** (para compilação local, se necessário)

**Verificar Docker:**
```bash
docker ps
```

Se o comando acima falhar, inicie o Docker Desktop e aguarde até que esteja totalmente inicializado.

### Comandos para Executar a API

```bash
# 1. Clone o repositório
git clone https://github.com/saulocapistrano/search-credit.git
cd search-credit

# 2. Criar rede Docker (se não existir)
docker network create search-credit-network

# 3. Compilar o projeto
./mvnw clean package

# 4. Subir infraestrutura (PostgreSQL, Zookeeper, Kafka)
docker compose up -d postgres zookeeper kafka

# 5. Aguardar Kafka inicializar (10-30 segundos)
docker compose logs kafka | grep "started (kafka.server.KafkaServer)"

# 6. Subir a API
docker compose up -d search-credit

# 7. Verificar logs da API
docker compose logs -f search-credit
```

**Aguarde até ver:** `Started SearchCreditApplication` nos logs.

### Acessar a API

- **Swagger UI:** http://localhost:8189/swagger-ui.html
- **API Base:** http://localhost:8189

### Testar os Endpoints

```bash
# Consultar créditos por NFS-e
curl http://localhost:8189/api/creditos/7891011

# Consultar crédito específico
curl http://localhost:8189/api/creditos/credito/123456
```

## Execução da API Isoladamente

A API pode ser executada isoladamente para testes via Swagger ou curl. A infraestrutura necessária (PostgreSQL, Kafka) é provisionada automaticamente via Docker Compose.

## Endpoints Disponíveis

### GET /api/creditos/{numeroNfse}

Retorna lista de créditos associados a um número de NFS-e.

**Exemplo:**
```bash
GET http://localhost:8189/api/creditos/7891011
```

**Resposta:**
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
  }
]
```

### GET /api/creditos/credito/{numeroCredito}

Retorna detalhes de um crédito específico.

**Exemplo:**
```bash
GET http://localhost:8189/api/creditos/credito/123456
```

**Resposta:**
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

## Execução do Ecossistema Completo

Para testar o sistema completo (Backend + Frontend), execute os projetos abaixo na ordem indicada.

### 1. Backend (Este Projeto)

Siga os passos da seção "Execução Rápida" acima.

### 2. Frontend Angular

```bash
git clone https://github.com/saulocapistrano/search-credit-frontend.git
cd search-credit-frontend
docker compose up -d --build
```

**Repositório:** https://github.com/saulocapistrano/search-credit-frontend

**Responsabilidades:**
- Interface web para consulta de créditos
- Consulta por NFS-e ou número do crédito
- Tabela responsiva de resultados
- Porta: `4200`

**Acessar:** http://localhost:4200

## Serviço de Análise (Opcional)

O worker Kafka é um serviço adicional que demonstra arquitetura de microsserviços e processamento assíncrono. **Não é necessário** para avaliação do desafio técnico.

### Worker Kafka (Opcional)

```bash
git clone https://github.com/saulocapistrano/credito-analise-worker.git
cd credito-analise-worker
./mvnw clean package
docker compose up -d worker
```

**Repositório:** https://github.com/saulocapistrano/credito-analise-worker

**Responsabilidades:**
- Consome eventos Kafka do tópico `consulta-creditos-topic`
- Processa eventos de consulta de forma assíncrona
- Porta: `8081`

**Observação:** Este serviço é **opcional** e demonstra conhecimento em arquitetura de microsserviços e comunicação assíncrona.

## Testes Automatizados

### Executar Testes

```bash
./mvnw clean test
```

**Cobertura:**
- Testes unitários do `CreditoService` (11 testes)
- JUnit 5 e Mockito
- Padrão Arrange/Act/Assert
- Testes isolados sem dependências externas

## Tecnologias e Recursos

### Stack Tecnológico

- **Java 17**
- **Spring Boot 3.3.5**
- **PostgreSQL 15** - Banco de dados relacional
- **Apache Kafka** - Comunicação assíncrona
- **Liquibase** - Migrações de banco de dados
- **Docker & Docker Compose** - Containerização
- **JUnit 5** - Testes unitários
- **Mockito** - Mocks para testes

### Arquitetura

O projeto segue **Clean Architecture** e **Domain-Driven Design (DDD)**:

- **Domain**: Entidades e interfaces de repositório (regras de negócio puras)
- **Application**: DTOs e serviços de aplicação (casos de uso)
- **Infrastructure**: Implementações concretas (JPA, Kafka)
- **Interfaces**: Controllers REST (camada de apresentação)

### Comunicação Assíncrona

A API publica eventos Kafka no tópico `consulta-creditos-topic` sempre que uma consulta é realizada:

- **Producer**: `search-credit` publica eventos JSON
- **Consumer**: `credito-analise-worker` (opcional) processa eventos assíncronos
- **Serialização**: JSON via `JsonSerializer`
- **Tópico**: `consulta-creditos-topic`

### Padrões de Projeto

- **MVC** - Separação Controller/Service/Repository
- **Repository Pattern** - Abstração de acesso a dados
- **DTO Pattern** - Transferência de dados entre camadas
- **Dependency Injection** - Injeção via Spring
- **Builder Pattern** - Construção de objetos (Lombok)

## Comandos Úteis

### Verificar Status dos Serviços

```bash
docker compose ps
```

### Ver Logs

```bash
docker compose logs -f search-credit
docker compose logs -f kafka
docker compose logs -f postgres
```

### Parar Todos os Serviços

```bash
docker compose down
```

### Parar e Remover Volumes (Limpar Banco)

```bash
docker compose down -v
```

**Atenção:** Isso apagará todos os dados do PostgreSQL.

### Reiniciar um Serviço

```bash
docker compose restart search-credit
```

## Troubleshooting

### Docker Desktop não está rodando

**Sintoma:** `Cannot connect to the Docker daemon`

**Solução:** Inicie o Docker Desktop e aguarde até que esteja totalmente inicializado.

### Porta já está em uso

**Sintoma:** `Bind for 0.0.0.0:8189 failed: port is already allocated`

**Solução:** Identifique e pare o processo usando a porta ou altere a porta no `docker-compose.yml`.

### API não consegue conectar ao PostgreSQL

**Sintoma:** `Connection to search-credit-postgres:5432 refused`

**Solução:**
1. Verifique se PostgreSQL está rodando: `docker compose ps postgres`
2. Aguarde alguns segundos e verifique os logs: `docker compose logs postgres`

### API não consegue conectar ao Kafka

**Sintoma:** `Bootstrap broker search-credit-kafka:9092 disconnected`

**Solução:**
1. Verifique se Kafka está rodando: `docker compose ps kafka`
2. Aguarde Kafka inicializar completamente (10-30 segundos)
3. Verifique logs: `docker compose logs kafka`

### Rede Docker não existe

**Sintoma:** `network search-credit-network not found`

**Solução:**
```bash
docker network create search-credit-network
```

## Estrutura do Projeto

```
search-credit/
├── src/
│   ├── main/
│   │   ├── java/br/com/searchcredit/
│   │   │   ├── application/      # DTOs e serviços
│   │   │   ├── domain/           # Entidades e interfaces
│   │   │   ├── infrastructure/   # JPA e Kafka
│   │   │   ├── interfaces/       # Controllers REST
│   │   │   └── SearchCreditApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/changelog/     # Migrações Liquibase
│   └── test/
│       └── java/                 # Testes unitários
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

## Links dos Repositórios

- **Backend (Este projeto):** https://github.com/saulocapistrano/search-credit
- **Frontend:** https://github.com/saulocapistrano/search-credit-frontend
- **Worker:** https://github.com/saulocapistrano/credito-analise-worker


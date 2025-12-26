# Search Credit - API de Consulta e Análise de Créditos

## Visão Geral

Sistema de consulta e análise de créditos constituídos desenvolvido como desafio técnico para vaga backend Java. A API permite consultar créditos por número de NFSe ou por número de crédito, seguindo princípios de Clean Architecture e Domain-Driven Design (DDD).

### Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.3.5**
- **PostgreSQL 15** (banco de dados relacional)
- **Apache Kafka 3.6** (comunicação assíncrona)
- **Docker & Docker Compose** (orquestração de serviços)
- **Liquibase** (migrações de banco de dados)
- **MinIO** (armazenamento de objetos, simula S3)
- **JUnit 5** (testes unitários)
- **Mockito** (mocks para testes)
- **Testcontainers** (testes de integração)

## Arquitetura

O projeto segue os princípios de **Clean Architecture** e **Domain-Driven Design (DDD)**, organizando o código em camadas bem definidas:

- **Domain**: Entidades e interfaces de repositório (regras de negócio puras)
- **Application**: DTOs e serviços de aplicação (casos de uso)
- **Infrastructure**: Implementações concretas (JPA, Kafka, MinIO)

A comunicação assíncrona é realizada através do **Apache Kafka**, onde o `search-credit` atua como **producer**, publicando eventos de consulta de créditos no tópico `consulta-creditos-topic`.

## Como Executar o Projeto Principal

### Pré-requisitos

- Java 17 ou superior
- Maven 3.6+
- Docker e Docker Compose instalados

### Passos para Execução

1. **Clone o repositório:**
   ```bash
   git clone <url-do-repositorio>
   cd search-credit
   ```

2. **Suba a infraestrutura com Docker Compose:**
   ```bash
   docker-compose up -d
   ```
   
   Isso irá iniciar os seguintes serviços:
   - PostgreSQL na porta `5437`
   - Apache Kafka na porta `9094`
   - Zookeeper na porta `2182`

3. **Execute a aplicação:**
   ```bash
   ./mvnw spring-boot:run
   ```
   
   Ou, se preferir compilar primeiro:
   ```bash
   ./mvnw clean package
   java -jar target/search-credit-0.0.1-SNAPSHOT.jar
   ```

4. **A API estará disponível em:**
   ```
   http://localhost:8189
   ```

**Importante:** Este projeto funciona **totalmente de forma isolada**. Não é necessário executar outros serviços para que a API funcione completamente. A infraestrutura necessária (PostgreSQL, Kafka) é provisionada automaticamente via Docker Compose.

## Endpoints Disponíveis

### GET /api/creditos/{numeroNfse}

Consulta todos os créditos associados a um número de NFSe.

**Exemplo de requisição:**
```bash
GET http://localhost:8189/api/creditos/NFSE123456
```

**Exemplo de resposta:**
```json
[
  {
    "numeroCredito": "CRED001",
    "numeroNfse": "NFSE123456",
    "dataConstituicao": "2024-01-15",
    "valorIssqn": 1500.00,
    "tipoCredito": "ISS",
    "simplesNacional": "Sim",
    "aliquota": 5.0,
    "valorFaturado": 30000.00,
    "valorDeducao": 5000.00,
    "baseCalculo": 25000.00
  }
]
```

### GET /api/creditos/credito/{numeroCredito}

Consulta um crédito específico pelo seu número.

**Exemplo de requisição:**
```bash
GET http://localhost:8189/api/creditos/credito/CRED001
```

**Exemplo de resposta:**
```json
{
  "numeroCredito": "CRED001",
  "numeroNfse": "NFSE123456",
  "dataConstituicao": "2024-01-15",
  "valorIssqn": 1500.00,
  "tipoCredito": "ISS",
  "simplesNacional": "Sim",
  "aliquota": 5.0,
  "valorFaturado": 30000.00,
  "valorDeducao": 5000.00,
  "baseCalculo": 25000.00
}
```

## Testes Automatizados

O projeto possui testes unitários e de integração utilizando JUnit 5, Mockito e Testcontainers.

### Executar os Testes

```bash
./mvnw clean test
```

Os testes incluem:
- Testes unitários de serviços e componentes
- Testes de controller (endpoints REST)
- Testes de integração com banco de dados e Kafka usando Testcontainers

## Serviços Opcionais (Extra para Avaliação Arquitetural)

Para demonstrar arquitetura de microsserviços e comunicação assíncrona, existem dois projetos adicionais **opcionais**:

### 1. credito-analise-worker

**Repositório:** https://github.com/saulocapistrano/credito-analise-worker.git

Este serviço é responsável por:
- Consumir eventos Kafka de solicitação de crédito publicados pelo `search-credit`
- Simular análise automática de crédito
- Processar aprovação ou reprovação de solicitações

### 2. search-credit-frontend

Frontend desenvolvido em Angular para interface de usuário.

**Observação:** Estes serviços são **opcionais** e não são necessários para a execução e avaliação do projeto principal `search-credit`. O avaliador não é obrigado a clonar ou executar esses projetos adicionais.

## Comunicação Assíncrona (Kafka)

O `search-credit` atua como **producer** no ecossistema Kafka, publicando eventos no tópico `consulta-creditos-topic` sempre que consultas ou solicitações de crédito são realizadas.

O serviço `credito-analise-worker` (opcional) consome esses eventos para realizar análises automáticas de crédito, demonstrando um padrão de arquitetura de microsserviços com comunicação assíncrona.

**Configuração Kafka:**
- Bootstrap servers: `localhost:9094`
- Tópico padrão: `consulta-creditos-topic`
- Serializadores: String (key e value)

## Armazenamento de Arquivos (MinIO)

O projeto utiliza **MinIO** como solução de armazenamento de objetos, simulando serviços como Amazon S3 ou Azure Blob Storage. O MinIO é usado para armazenar documentos associados a solicitações de crédito, permitindo upload e download de arquivos relacionados às análises.

**Nota:** O MinIO deve ser configurado no `docker-compose.yml` caso ainda não esteja presente, ou pode ser executado separadamente conforme necessário.

## Observações Finais

Este desafio técnico foca principalmente no desenvolvimento **backend** em Java, demonstrando:

- Arquitetura limpa e organização de código
- Padrões de design e boas práticas
- Integração com banco de dados relacional
- Comunicação assíncrona via mensageria
- Testes automatizados



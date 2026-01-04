# Search Credit — API de Gestão e Análise de Créditos (Event-Driven)

API REST desenvolvida em **Spring Boot** para **gestão do ciclo de vida de créditos**, incluindo:
- Consulta de créditos
- Criação de solicitações
- Análise manual
- Análise automática assíncrona (simulada)
- Persistência consistente e idempotente

O projeto foi desenvolvido com foco em **arquitetura orientada a eventos**, separação de responsabilidades e boas práticas de engenharia de software.

---

## Repositórios do Ecossistema

- **Backend (API):** https://github.com/saulocapistrano/search-credit
- **Frontend (Angular):** https://github.com/saulocapistrano/search-credit-frontend
- **Worker (Kafka):** https://github.com/saulocapistrano/credito-analise-worker

---

## Subindo o ecossistema (opcional)

### Frontend (Angular)

```bash
git clone https://github.com/saulocapistrano/search-credit-frontend.git
cd search-credit-frontend
docker compose up -d --build
```

### Worker (Kafka)

```bash
git clone https://github.com/saulocapistrano/credito-analise-worker.git
cd credito-analise-worker
docker compose up -d
```

---

## Arquitetura Geral

- A API é a **dona do estado** do crédito
- Toda decisão automática é **assíncrona**
- A análise manual (Admin Full) **tem precedência**
- Comunicação entre serviços via **Apache Kafka**
- Idempotência garantida no consumo de eventos

---

## Fluxo Principal

```
POST /api/creditos
↓
SolicitacaoCreditoEvent
↓
Kafka
↓
credito-analise-worker
↓
(simulação de decisão)
↓
CreditoAnalisadoEvent
↓
Kafka
↓
search-credit API
↓
Atualiza status (somente se ainda EM_ANALISE)
```

---

## Regras de Negócio Importantes

- Crédito nasce com status `EM_ANALISE`
- Apenas `APROVADO` ou `REPROVADO` são estados finais
- Decisão automática:
  - Só é aplicada se o status ainda for `EM_ANALISE`
  - É idempotente (eventos duplicados são ignorados)
- Decisão manual (Admin Full):
  - Sempre tem precedência
  - Bloqueia qualquer decisão automática posterior

---

## Tecnologias

- Java 17
- Spring Boot 3
- Spring Data JPA
- Apache Kafka
- PostgreSQL
- Liquibase
- Docker / Docker Compose
- JUnit 5 / Mockito

---

## Subindo a API

```bash
git clone https://github.com/saulocapistrano/search-credit.git
cd search-credit

./mvnw clean package
./bootstrap.sh # cria a rede e sobe os containers da API (infra + app)

```

### Acessos

- **API:** http://localhost:8189
- **Swagger:** http://localhost:8189/swagger-ui.html
- **Kafka UI:** http://localhost:8090

---

## Comunicação Assíncrona

Eventos utilizados:

- `ConsultaCreditoConsumer`
  - Consumidor Kafka responsável por processar consultas de crédito
- `SolicitacaoCreditoEvent`
  - Emitido ao criar uma solicitação
- `CreditoAnalisadoEvent`
  - Consumido pela API
  - Aplicado somente se o crédito ainda estiver `EM_ANALISE`

---

## Endpoints principais

- `POST /api/creditos` (multipart)
  - Part `credito` (JSON)
  - Part `comprovante` (opcional)
- `PUT /api/creditos/{id}/analise`
  - Apenas `EM_ANALISE` pode ser analisado
  - Novo status somente `APROVADO` ou `REPROVADO`
- `GET /api/creditos/next-numero-credito`
- `GET /api/creditos/next-numero-nfse`

---

## Testes

```bash
./mvnw clean test
```

Cobertura inclui análise manual, consumo Kafka e idempotência.

---

## Observação importante

O worker não realiza análise real de crédito.
Ele existe para demonstrar processamento assíncrono, event-driven e idempotência. Foi implementado com foco em robustez e confiabilidade em ambientes distribuídos, garantindo entrega garantida de eventos mesmo em cenários de falhas. O worker de análise está implementando uma decisão randomica para aprovação/reprovação de crédito.



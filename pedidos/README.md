# pedidos

Microsserviço orquestrador do fluxo de compra do iCompras. É responsável por criar o pedido, validar cliente e produtos junto aos serviços correspondentes, simular a solicitação de pagamento, publicar o pedido pago no Kafka e manter o status do pedido atualizado conforme o restante do fluxo (faturamento e envio) acontece.

## Responsabilidades

- Criar pedidos, validando cliente e itens junto aos serviços `clientes` e `produtos` (via Feign/HTTP).
- Simular a solicitação de pagamento junto a um "serviço bancário" (mock interno).
- Receber o callback de confirmação de pagamento e, em caso de sucesso, publicar o evento no tópico `icompras.pedidos-pagos`.
- Consumir os tópicos `icompras.pedidos-faturados` e `icompras.pedidos-enviados` para atualizar o status do pedido, o link da nota fiscal e o código de rastreio.
- Expor o detalhamento completo do pedido (dados do cliente + itens com nome do produto).

## Tecnologias

- Spring Boot (Web, Data JPA, Kafka, OpenFeign)
- PostgreSQL
- Apache Kafka (produtor e consumidor)

## Configuração

Arquivo: `src/main/resources/application.properties`

| Propriedade | Valor padrão | Descrição |
|---|---|---|
| `server.port` | `8080` | Porta HTTP do serviço |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5555/icompraspedidos` | Conexão com o banco |
| `spring.kafka.bootstrap-servers` / `icompras.config.kafka.server-url` | `localhost:29092` | Endereço do broker Kafka |
| `spring.kafka.consumer.group-id` | `icompras-atualizacao-pedido` | Consumer group usado para atualização de status |
| `icompras.pedidos.clients.produtos.url` | `http://localhost:8081/produtos` | URL base do serviço `produtos` |
| `icompras.pedidos.clients.clientes.url` | `http://localhost:8082/clientes` | URL base do serviço `clientes` |
| `icompras.config.kafka.topics.pedidos-pagos` | `icompras.pedidos-pagos` | Tópico onde este serviço **produz** |
| `icompras.config.kafka.topics.pedidos-faturados` | `icompras.pedidos-faturados` | Tópico que este serviço **consome** |
| `icompras.config.kafka.topics.pedidos-enviados` | `icompras.pedidos-enviados` | Tópico que este serviço **consome** |

## Pré-requisitos para rodar

1. PostgreSQL no ar na porta `5555`, com o banco `icompraspedidos` e as tabelas `pedido`/`item_pedido` já criadas (veja `icompras-servicos/database/schema.sql`).
2. Kafka no ar na porta `29092`.
3. **Os tópicos `icompras.pedidos-pagos`, `icompras.pedidos-faturados` e `icompras.pedidos-enviados` já criados no Kafka UI** — passo manual descrito no [README geral](../README.md#passo-3--criar-os-tópicos-no-kafka-ui--etapa-manual-obrigatória) e no [README de infraestrutura](../icompras-servicos/README.md).
4. Os serviços **clientes** (porta `8082`) e **produtos** (porta `8081`) no ar, pois são chamados de forma síncrona ao criar um pedido.

## Como rodar

```bash
cd pedidos
./mvnw spring-boot:run
```

O serviço sobe em `http://localhost:8080`.

## Modelo de dados

Tabela `pedido`:

| Campo | Tipo | Observação |
|---|---|---|
| `codigo` | `serial` | chave primária |
| `codigo_cliente` | `bigint` | referência ao cliente |
| `data_pedido` | `timestamp` | data/hora do pedido |
| `chave_pagamento` | `text` | chave retornada pela simulação de pagamento |
| `observacoes` | `text` | mensagens de erro/observações |
| `status` | `varchar(20)` | um dos valores de `StatusPedido` (veja abaixo) |
| `total` | `decimal(16,2)` | valor total do pedido |
| `codigo_rastreio` | `varchar(255)` | preenchido pelo serviço `logistica` |
| `url_nf` | `text` | URL da nota fiscal, preenchida pelo serviço `faturamento` |

Tabela `item_pedido`: código, código do pedido, código do produto, quantidade e valor unitário.

### Status do pedido (`StatusPedido`)

`REALIZADO` → `PAGO` → `FATURADO` → `ENVIADO`, com o desvio possível `ERRO_PAGAMENTO` caso o pagamento falhe.

### Tipos de pagamento (`TipoPagamento`)

`DEBIT`, `CREDIT`, `PIX`.

## Endpoints

### Criar pedido

```
POST /pedidos
```

**Body:**
```json
{
  "codigoCliente": 1,
  "dadosPagamento": {
    "dados": "numero-do-cartao-ou-chave-pix",
    "tipoPagamento": "CREDIT"
  },
  "itens": [
    { "codigoProduto": 1, "quantidade": 2, "valorUnitario": 350.00 }
  ]
}
```

O serviço valida o cliente e cada item junto aos serviços `clientes`/`produtos`. Se o cliente ou algum produto não existir ou estiver inativo, retorna erro de validação. Se tudo estiver válido, o pedido é persistido e uma solicitação de pagamento é disparada (simulada internamente).

**Respostas:**
- `200 OK` com o código do pedido criado.
- `400 Bad Request` com detalhe do campo inválido, em caso de erro de validação.

### Adicionar novo pagamento a um pedido existente

```
POST /pedidos/pagamentos
```

Usado para reprocessar o pagamento de um pedido já existente (por exemplo, após um `ERRO_PAGAMENTO`), gerando uma nova chave de pagamento.

**Body:**
```json
{
  "codigoPedido": 1,
  "dados": "novo-numero-do-cartao",
  "tipoPagamento": "PIX"
}
```

**Respostas:**
- `204 No Content` em caso de sucesso.
- `400 Bad Request` se o pedido não existir.

### Callback de confirmação de pagamento

```
POST /pedidos/callback-pagamentos
```

**Header obrigatório:** `apiKey`

**Body:**
```json
{
  "codigo": 1,
  "chavePagamento": "chave-retornada-na-criação-do-pedido",
  "status": true,
  "observacoes": "Pagamento aprovado"
}
```

Este endpoint simula o retorno de um serviço bancário/gateway de pagamento. Se `status` for `true`, o pedido passa para `PAGO`, os dados completos são carregados e o evento é publicado em `icompras.pedidos-pagos`. Se `false`, o pedido passa para `ERRO_PAGAMENTO` com as `observacoes` informadas.

**Resposta:** `200 OK`.

### Consultar detalhes do pedido

```
GET /pedidos/{codigo}
```

Retorna o pedido com os dados completos do cliente e dos itens (com nome do produto), status atual, URL da nota fiscal (quando faturado) e código de rastreio (quando enviado).

**Respostas:** `200 OK`, ou `404 Not Found` se o pedido não existir.

## Integrações

### Chamadas síncronas (Feign / HTTP)

- `GET {clientes}/{codigo}` — valida e carrega os dados do cliente.
- `GET {produtos}/{codigo}` — valida e carrega os dados de cada produto do pedido.

### Kafka — produção

- **`icompras.pedidos-pagos`** — publicado após a confirmação de pagamento via callback, contendo os detalhes completos do pedido (cliente e itens), consumido pelo serviço **faturamento**.

### Kafka — consumo

- **`icompras.pedidos-faturados`** e **`icompras.pedidos-enviados`** — consumidos (mesmo listener, `AtualizacaoStatusPedidoSubscriber`, consumer group `icompras-atualizacao-pedido`) para atualizar o status do pedido, a URL da nota fiscal e o código de rastreio conforme os eventos chegam dos serviços **faturamento** e **logística**.

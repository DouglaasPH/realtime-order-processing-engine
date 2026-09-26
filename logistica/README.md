# logistica

Microsserviço responsável por preparar o envio de um pedido já faturado, gerando um código de rastreio e notificando o restante do fluxo de que o pedido foi enviado.

## Responsabilidades

- Consumir o tópico `icompras.pedidos-faturados`, publicado pelo serviço `faturamento`.
- Gerar um código de rastreio no formato `AB123456789BR`.
- Publicar o evento de pedido enviado no tópico `icompras.pedidos-enviados`, com o status `ENVIADO` e o código de rastreio.

## Tecnologias

- Spring Boot (Kafka)
- Apache Kafka (produtor e consumidor)

## Configuração

Arquivo: `src/main/resources/application.properties`

| Propriedade | Valor padrão | Descrição |
|---|---|---|
| `server.port` | `8084` | Porta HTTP do serviço (não expõe endpoints REST de negócio; porta reservada para o contexto Spring) |
| `spring.kafka.bootstrap-servers` | `localhost:29092` | Endereço do broker Kafka |
| `spring.kafka.consumer.group-id` | `icompras-logistica` | Consumer group |
| `icompras.config.kafka.topics.pedidos-faturados` | `icompras.pedidos-faturados` | Tópico que este serviço **consome** |
| `icompras.config.kafka.topics.pedidos-enviados` | `icompras.pedidos-enviados` | Tópico onde este serviço **produz** |

## Pré-requisitos para rodar

1. Kafka no ar na porta `29092`.
2. **Tópicos `icompras.pedidos-faturados` e `icompras.pedidos-enviados` já criados no Kafka UI** (1 partição cada) — passo manual descrito no [README geral](../README.md#passo-3--criar-os-tópicos-no-kafka-ui--etapa-manual-obrigatória).

## Como rodar

```bash
cd logistica
./mvnw spring-boot:run
```

O serviço sobe em `http://localhost:8084`. Ele não expõe endpoints REST de negócio — toda a sua lógica é orientada a eventos Kafka.

## Fluxo de envio

1. `FaturamentoSubscriber` consome uma mensagem do tópico `icompras.pedidos-faturados`, contendo o código do pedido e a URL da nota fiscal.
2. `EnvioPedidoService` gera um código de rastreio aleatório no formato `AB123456789BR` (duas letras + nove dígitos + `BR`).
3. `EnvioPedidoPublisher` publica no tópico `icompras.pedidos-enviados` o código do pedido, o status `ENVIADO` e o código de rastreio gerado.

## Integrações

### Kafka — consumo

- **`icompras.pedidos-faturados`** — dispara a geração do código de rastreio e o início do processo de envio.

### Kafka — produção

- **`icompras.pedidos-enviados`** — publicado após o código de rastreio ser gerado, contendo `codigo` do pedido, status `ENVIADO` e `codigoRastreio`. Consumido pelo serviço **pedidos** para atualização final de status.

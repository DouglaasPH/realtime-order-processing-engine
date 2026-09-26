# faturamento

Microsserviço responsável por gerar a nota fiscal (PDF) de um pedido pago, armazená-la em um bucket MinIO e notificar o restante do fluxo de que o pedido foi faturado.

## Responsabilidades

- Consumir o tópico `icompras.pedidos-pagos`, publicado pelo serviço `pedidos`.
- Gerar o PDF da nota fiscal a partir de um template JasperReports (`nota-fiscal.jrxml`), usando os dados do cliente, do pedido e dos itens.
- Fazer upload do PDF gerado para o bucket MinIO `icompras.faturas`.
- Publicar o evento de pedido faturado no tópico `icompras.pedidos-faturados`, contendo a URL da nota fiscal.
- Expor endpoints auxiliares de upload/consulta de arquivos no bucket.

## Tecnologias

- Spring Boot (Kafka)
- Apache Kafka (produtor e consumidor)
- MinIO (armazenamento de objetos S3-compatível)
- JasperReports (geração de PDF)

## Configuração

Arquivo: `src/main/resources/application.properties`

| Propriedade | Valor padrão | Descrição |
|---|---|---|
| `server.port` | `8083` | Porta HTTP do serviço |
| `spring.kafka.bootstrap-servers` | `localhost:29092` | Endereço do broker Kafka |
| `spring.kafka.consumer.group-id` | `icompras-faturamento` | Consumer group |
| `icompras.config.kafka.topics.pedidos-pagos` | `icompras.pedidos-pagos` | Tópico que este serviço **consome** |
| `icompras.config.kafka.topics.pedidos-faturados` | `icompras.pedidos-faturados` | Tópico onde este serviço **produz** |
| `minio.url` | `http://localhost:9000` | Endpoint da API do MinIO |
| `minio.access-key` / `minio.secret-key` | `minioadmin` / `minioadmin123` | Credenciais do MinIO |
| `minio.bucket-name` | `icompras.faturas` | Bucket onde os PDFs das notas fiscais são armazenados |

## Pré-requisitos para rodar

1. Kafka no ar na porta `29092`.
2. **Tópicos `icompras.pedidos-pagos` e `icompras.pedidos-faturados` já criados no Kafka UI** (1 partição cada) — passo manual descrito no [README geral](../README.md#passo-3--criar-os-tópicos-no-kafka-ui--etapa-manual-obrigatória).
3. MinIO no ar na porta `9000`.
4. **Bucket `icompras.faturas` já criado no console do MinIO** — passo manual descrito no [README geral](../README.md#passo-5--criar-o-bucket-no-minio--etapa-manual-obrigatória) e no [README de infraestrutura](../icompras-servicos/README.md).

## Como rodar

```bash
cd faturamento
./mvnw spring-boot:run
```

O serviço sobe em `http://localhost:8083`.

## Fluxo de geração da nota fiscal

1. `PedidoPagoSubscriber` consome uma mensagem do tópico `icompras.pedidos-pagos`.
2. `GeradorNotaFiscalService` chama `NotaFiscalService`, que preenche o template Jasper (`reports/nota-fiscal.jrxml`) com nome, CPF, endereço e contato do cliente, data e total do pedido, e a lista de itens — e exporta o resultado como PDF.
3. O PDF é enviado ao bucket `icompras.faturas` via `BucketService`, com o nome `notafiscal_pedido_{codigo}.pdf`.
4. A URL do arquivo é obtida e publicada no tópico `icompras.pedidos-faturados` (`FaturamentoPublisher`), junto com o código do pedido e o status `FATURADO`.

## Endpoints

Endpoints auxiliares para interagir diretamente com o bucket (geralmente não usados no fluxo principal, que é orientado a eventos, mas úteis para depuração):

### Upload de arquivo

```
POST /bucket
```

**Body:** `multipart/form-data` com o campo `file`.

**Respostas:**
- `200 OK` com a mensagem de confirmação.
- `500 Internal Server Error` em caso de falha no upload.

### Obter URL de um arquivo

```
GET /bucket?filename={nome-do-arquivo}
```

**Respostas:**
- `200 OK` com a URL assinada/pública do arquivo.
- `500 Internal Server Error` em caso de falha.

## Integrações

### Kafka — consumo

- **`icompras.pedidos-pagos`** — dispara a geração da nota fiscal.

### Kafka — produção

- **`icompras.pedidos-faturados`** — publicado após a nota fiscal ser gerada e armazenada, contendo `codigo` do pedido, status `FATURADO` e `urlNotaFiscal`. Consumido pelos serviços **pedidos** (atualização de status) e **logistica** (início do fluxo de envio).

### MinIO

- Bucket **`icompras.faturas`** — precisa existir previamente (criação manual, veja pré-requisitos acima).

# icompras-servicos — Infraestrutura

Este módulo **não é um microsserviço de negócio**: ele reúne os arquivos `docker-compose.yml` usados para provisionar a infraestrutura compartilhada pelos demais serviços do projeto **iCompras** (Kafka, PostgreSQL e MinIO). O código Java presente aqui é apenas um projeto Spring Boot base, sem lógica de negócio.

## Estrutura

```
icompras-servicos/
├── broker/
│   └── docker-compose.yml   # Zookeeper + Kafka + Kafka UI
├── bucket/
│   └── docker-compose.yml   # MinIO
└── database/
    ├── docker-compose.yml   # PostgreSQL
    └── schema.sql           # script de criação dos bancos/tabelas
```

## broker/ — Apache Kafka

Sobe:
- **Zookeeper** — porta `22181`
- **Kafka** — porta interna `9092` (rede docker) e porta externa `29092` (para os serviços rodando na sua máquina)
- **Kafka UI** (provectuslabs) — porta `8090`, interface web para gerenciar o cluster

```bash
cd icompras-servicos/broker
docker compose up -d
```

### ⚠️ Criação manual dos tópicos

Depois que o Kafka UI estiver disponível em **http://localhost:8090**, crie manualmente os três tópicos usados pelo sistema (o Kafka Auto Topic Creation não está habilitado para o fluxo do projeto, então essa etapa é obrigatória antes de testar o fluxo de pedidos):

1. Acesse **http://localhost:8090** e selecione o cluster `local`.
2. Vá em **Topics** → **Add a Topic**.
3. Crie cada um dos tópicos abaixo com **Number of partitions = 1**:

| Tópico | Partitions |
|---|---|
| `icompras.pedidos-pagos` | 1 |
| `icompras.pedidos-faturados` | 1 |
| `icompras.pedidos-enviados` | 1 |

4. Confirme em **Create Topic** para cada um.

## bucket/ — MinIO

Sobe um MinIO com:
- API S3 — porta `9000`
- Console web — porta `9001`
- Credenciais: `minioadmin` / `minioadmin123`

```bash
cd icompras-servicos/bucket
docker compose up -d
```

### ⚠️ Criação manual do bucket

O serviço `faturamento` grava os PDFs das notas fiscais no bucket `icompras.faturas`, que precisa existir antes de rodar o fluxo:

1. Acesse o console em **http://localhost:9001** e faça login (`minioadmin` / `minioadmin123`).
2. Vá em **Buckets** → **Create Bucket**.
3. Nome do bucket: **`icompras.faturas`**.
4. Clique em **Create Bucket**.

## database/ — PostgreSQL

Sobe um PostgreSQL na porta `5555` (usuário/senha `postgres`/`postgres`), com os bancos `icomprasprodutos`, `icompraspedidos` e `icomprasclientes`.

```bash
cd icompras-servicos/database
docker compose up -d
```

Depois de subir o container, execute o `schema.sql` desta pasta contra o banco para criar as tabelas:

- `produtos` (dentro de `icomprasprodutos`)
- `clientes` (dentro de `icomprasclientes`)
- `pedido` e `item_pedido` (dentro de `icompraspedidos`)

Você pode rodar o script com o cliente `psql`, DBeaver, IntelliJ Database Tool, ou qualquer cliente SQL de sua preferência, conectando em `localhost:5555`.

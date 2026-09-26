# produtos

Microsserviço responsável pelo cadastro (CRUD) de produtos do iCompras. É consultado pelo serviço **pedidos** para validar se um produto existe e está ativo antes de permitir a criação de um pedido.

## Responsabilidades

- Cadastrar, consultar e remover (soft delete lógico via flag `ativo`) produtos.
- Expor os dados do produto via API REST para o serviço `pedidos`.

## Tecnologias

- Spring Boot (Web, Data JPA)
- PostgreSQL

## Configuração

Arquivo: `src/main/resources/application.properties`

| Propriedade | Valor padrão | Descrição |
|---|---|---|
| `server.port` | `8081` | Porta HTTP do serviço |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5555/icomprasprodutos` | Conexão com o banco |
| `spring.datasource.username` / `password` | `postgres` / `postgres` | Credenciais do banco |

## Pré-requisitos para rodar

1. PostgreSQL no ar na porta `5555`, com o banco `icomprasprodutos` e a tabela `produtos` já criados (veja `icompras-servicos/database/schema.sql`, descrito no [README de infraestrutura](../icompras-servicos/README.md)).

## Como rodar

```bash
cd produtos
./mvnw spring-boot:run
```

O serviço sobe em `http://localhost:8081`.

## Modelo de dados

Tabela `produtos`:

| Campo | Tipo | Observação |
|---|---|---|
| `codigo` | `serial` | chave primária, gerada automaticamente |
| `nome` | `varchar(100)` | obrigatório |
| `valor_unitario` | `decimal(16,2)` | obrigatório |
| `ativo` | `boolean` | definido automaticamente como `true` na criação |

## Endpoints

### Criar produto

```
POST /produtos
```

**Body:**
```json
{
  "nome": "Teclado mecânico",
  "valorUnitario": 350.00
}
```

**Resposta:** `200 OK` com o objeto do produto criado (incluindo o `codigo` gerado).

### Consultar produto

```
GET /produtos/{codigo}
```

**Resposta:** `200 OK` com os dados do produto, ou `404 Not Found` se não existir.

### Remover produto

```
DELETE /produtos/{codigo}
```

**Resposta:** `204 No Content`, ou `404 Not Found` se o produto não existir.

## Integração com outros serviços

O serviço **pedidos** chama `GET /produtos/{codigo}` (via Feign Client) para:
- Validar que cada item do pedido referencia um produto existente e `ativo`.
- Buscar o nome do produto para compor o detalhamento do pedido.

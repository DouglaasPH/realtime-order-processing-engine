# clientes

Microsserviço responsável pelo cadastro (CRUD) de clientes do iCompras. É consultado pelo serviço **pedidos** para validar se um cliente existe e está ativo antes de permitir a criação de um pedido.

## Responsabilidades

- Cadastrar, consultar e remover (soft delete lógico via flag `ativo`) clientes.
- Expor os dados do cliente via API REST para o serviço `pedidos`.

## Tecnologias

- Spring Boot (Web, Data JPA)
- PostgreSQL

## Configuração

Arquivo: `src/main/resources/application.properties`

| Propriedade | Valor padrão | Descrição |
|---|---|---|
| `server.port` | `8082` | Porta HTTP do serviço |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5555/icomprasclientes` | Conexão com o banco |
| `spring.datasource.username` / `password` | `postgres` / `postgres` | Credenciais do banco |

## Pré-requisitos para rodar

1. PostgreSQL no ar na porta `5555`, com o banco `icomprasclientes` e a tabela `clientes` já criados (veja `icompras-servicos/database/schema.sql`, descrito no [README de infraestrutura](../icompras-servicos/README.md)).

## Como rodar

```bash
cd clientes
./mvnw spring-boot:run
```

O serviço sobe em `http://localhost:8082`.

## Modelo de dados

Tabela `clientes`:

| Campo | Tipo | Observação |
|---|---|---|
| `codigo` | `serial` | chave primária, gerada automaticamente |
| `nome` | `varchar(150)` | obrigatório |
| `cpf` | `char(11)` | obrigatório |
| `logradouro` | `varchar(100)` | opcional |
| `numero` | `varchar(10)` | opcional |
| `bairro` | `varchar(100)` | opcional |
| `email` | `varchar(150)` | opcional |
| `telefone` | `varchar(20)` | opcional |
| `ativo` | `boolean` | definido automaticamente como `true` na criação |

## Endpoints

### Criar cliente

```
POST /clientes
```

**Body:**
```json
{
  "nome": "Maria Silva",
  "cpf": "12345678900",
  "logradouro": "Rua das Flores",
  "numero": "123",
  "bairro": "Centro",
  "email": "maria@email.com",
  "telefone": "11999998888"
}
```

**Resposta:** `200 OK` com o objeto do cliente criado (incluindo o `codigo` gerado).

### Consultar cliente

```
GET /clientes/{codigo}
```

**Resposta:** `200 OK` com os dados do cliente, ou `404 Not Found` se não existir.

### Remover cliente

```
DELETE /clientes/{codigo}
```

**Resposta:** `204 No Content`, ou `404 Not Found` se o cliente não existir.

## Integração com outros serviços

O serviço **pedidos** chama `GET /clientes/{codigo}` (via Feign Client) para:
- Validar que o cliente existe e está `ativo` antes de criar um pedido.
- Buscar os dados completos do cliente (nome, CPF, endereço) para exibir no detalhe do pedido e para compor a nota fiscal gerada pelo serviço **faturamento**.

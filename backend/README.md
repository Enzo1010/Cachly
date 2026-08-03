# Questly Backend

Backend da plataforma Questly, responsável pela API REST, regras de negócio,
persistência, validações e integração com o PostgreSQL.

## Tecnologias

- Java 21
- Spring Boot 4.1
- Maven Wrapper
- Spring Data JPA
- Bean Validation
- PostgreSQL
- Flyway

O pacote base da aplicação é:

```text
br.com.questly.backend
```

## Pré-requisitos

Para executar o backend localmente, instale:

- Java 21;
- PostgreSQL;
- Git;
- IntelliJ IDEA ou outra IDE compatível com Maven e Java.

## Configuração do PostgreSQL

Crie um banco vazio chamado `questly`:

```sql
CREATE DATABASE questly;
```

As tabelas não precisam ser criadas manualmente. O Flyway executa as migrations
automaticamente quando o backend é iniciado.

### Variáveis de ambiente

| Variável | Valor padrão |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/questly` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `postgres` |

Credenciais pessoais não devem ser adicionadas ao Git.

Exemplo no PowerShell:

```powershell
$env:DB_PASSWORD="sua_senha"
```

Exemplo no Git Bash:

```bash
export DB_PASSWORD="sua_senha"
```

No IntelliJ IDEA, configure as variáveis em:

```text
Run > Edit Configurations > Environment variables
```

## Executando o backend

Entre na pasta `backend` antes de executar os comandos.

### PowerShell ou Prompt de Comando

```powershell
.\mvnw.cmd spring-boot:run
```

### Git Bash, Linux ou macOS

```bash
./mvnw spring-boot:run
```

Por padrão, a API ficará disponível em:

```text
http://localhost:8080
```

## Executando os testes

No PowerShell ou Prompt de Comando:

```powershell
.\mvnw.cmd test
```

No Git Bash, Linux ou macOS:

```bash
./mvnw test
```

O teste de contexto utiliza o PostgreSQL configurado pelas variáveis de
ambiente. O banco deve estar acessível durante a execução da suíte completa.

## Flyway

As migrations ficam em:

```text
src/main/resources/db/migration
```

| Versão | Estrutura criada |
| --- | --- |
| `V1` | Categorias |
| `V2` | Usuários |
| `V3` | Questões |
| `V4` | Alternativas |
| `V5` | Tentativas de questão |

Uma migration aplicada não deve ser modificada. Mudanças futuras no banco devem
ser criadas em um novo arquivo versionado, como:

```text
V6__descricao_da_alteracao.sql
```

O Hibernate está configurado com `ddl-auto: validate`, portanto valida o schema
sem criar ou modificar tabelas.

## Health check

```http
GET /api/health
```

Resposta esperada:

```json
{
  "status": "UP",
  "application": "Questly"
}
```

## Endpoints disponíveis

### Categorias

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/api/categorias` | Cadastra uma categoria |
| `GET` | `/api/categorias` | Lista categorias ativas |
| `GET` | `/api/categorias/{id}` | Busca uma categoria pelo ID |
| `PUT` | `/api/categorias/{id}` | Atualiza uma categoria |
| `PATCH` | `/api/categorias/{id}/desativar` | Desativa uma categoria |

### Questões

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/api/questoes` | Cadastra uma questão |
| `GET` | `/api/questoes` | Lista questões ativas |
| `GET` | `/api/questoes/{id}` | Busca uma questão pelo ID |
| `PUT` | `/api/questoes/{id}` | Atualiza uma questão |
| `PATCH` | `/api/questoes/{id}/desativar` | Desativa uma questão |

As respostas de erro seguem um formato comum para validação, recurso não
encontrado e conflito de dados. A autenticação ainda não foi implementada.

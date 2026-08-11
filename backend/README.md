# Cachly Backend

Backend da plataforma Cachly, responsável pela API REST, regras de negócio,
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
br.com.cachly.backend
```

## Pré-requisitos

Para executar o backend localmente, instale:

- Java 21;
- PostgreSQL;
- Git;
- IntelliJ IDEA ou outra IDE compatível com Maven e Java.

## Configuração do PostgreSQL

Crie um banco vazio chamado `cachly`:

```sql
CREATE DATABASE cachly;
```

As tabelas não precisam ser criadas manualmente. O Flyway executa as migrations
automaticamente quando o backend é iniciado.

### Variáveis de ambiente

| Variável | Valor padrão |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/cachly` |
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
| `V6` | Campo de ativação das alternativas |

Uma migration aplicada não deve ser modificada. Mudanças futuras no banco devem
ser criadas em um novo arquivo versionado, como:

```text
V7__descricao_da_alteracao.sql
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
  "application": "Cachly"
}
```

## Endpoints disponíveis

### Categorias

| Método | Endpoint | Descrição | Acesso |
| --- | --- | --- | --- |
| `POST` | `/api/categorias` | Cadastra uma categoria | **ADMIN** |
| `GET` | `/api/categorias` | Lista categorias ativas | Autenticado |
| `GET` | `/api/categorias/{id}` | Busca uma categoria pelo ID | Autenticado |
| `PUT` | `/api/categorias/{id}` | Atualiza uma categoria | **ADMIN** |
| `PATCH` | `/api/categorias/{id}/desativar` | Desativa uma categoria | **ADMIN** |

### Questões

| Método | Endpoint | Descrição | Acesso |
| --- | --- | --- | --- |
| `POST` | `/api/questoes` | Cadastra uma questão | **ADMIN** |
| `GET` | `/api/questoes` | Lista questões ativas | Autenticado |
| `GET` | `/api/questoes/estudo` | Busca um lote de questões ativas | Autenticado |
| `GET` | `/api/questoes/{id}` | Busca uma questão pelo ID | Autenticado |
| `PUT` | `/api/questoes/{id}` | Atualiza uma questão | **ADMIN** |
| `PATCH` | `/api/questoes/{id}/desativar` | Desativa uma questão | **ADMIN** |

### Alternativas

| Método | Endpoint | Descrição | Acesso |
| --- | --- | --- | --- |
| `POST` | `/api/questoes/{questaoId}/alternativas` | Cadastra uma alternativa | **ADMIN** |
| `GET` | `/api/questoes/{questaoId}/alternativas` | Lista alternativas ativas em ordem | Autenticado |
| `GET` | `/api/questoes/{questaoId}/alternativas/{id}` | Busca uma alternativa pelo ID | Autenticado |
| `PUT` | `/api/questoes/{questaoId}/alternativas/{id}` | Atualiza uma alternativa | **ADMIN** |
| `PATCH` | `/api/questoes/{questaoId}/alternativas/{id}/desativar` | Desativa uma alternativa | **ADMIN** |

### Alunos

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/api/alunos` | Cadastra um novo aluno |
| `GET` | `/api/alunos/me` | Busca os dados do aluno autenticado |

### Autenticação

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Realiza o login e retorna o token JWT |

### Respostas

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/api/respostas` | Registra a tentativa de resposta e retorna a correção |

As respostas de erro seguem um formato comum para validação, recurso não
encontrado e conflito de dados. A autenticação é stateless baseada em JWT.

# Cachly Backend

Backend da plataforma Cachly, responsável pela API REST, regras de negócio,
persistência, validações e integração com o PostgreSQL.

## Tecnologias

- Java 21
- Spring Boot 4.1
- Maven Wrapper
- Spring Security (JWT stateless)
- Spring Data JPA
- Bean Validation
- PostgreSQL
- Flyway
- SpringDoc OpenAPI (Swagger UI)

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

| Variável              | Valor padrão                              |
|-----------------------|-------------------------------------------|
| `DB_URL`              | `jdbc:postgresql://localhost:5432/cachly` |
| `DB_USERNAME`         | `postgres`                                |
| `DB_PASSWORD`         | `postgres`                                |
| `JWT_SECRET`          | valor padrão de desenvolvimento           |
| `JWT_EXPIRATION_HOURS`| `24`                                      |

Credenciais pessoais não devem ser adicionadas ao Git.

Exemplo no PowerShell:

```powershell
$env:DB_PASSWORD="sua_senha"
```

Exemplo no Git Bash ou macOS:

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

## Documentação interativa (Swagger UI)

Com a aplicação em execução, acesse:

```text
http://localhost:8080/swagger-ui.html
```

A documentação lista todos os endpoints disponíveis com seus parâmetros, corpos
de requisição e respostas esperadas. Para testar rotas autenticadas, clique em
**Authorize** e informe o token JWT obtido no login.

## Executando os testes

No PowerShell ou Prompt de Comando:

```powershell
.\mvnw.cmd test
```

No Git Bash, Linux ou macOS:

```bash
./mvnw test
```

O banco de dados deve estar acessível durante a execução dos testes de integração.

## Flyway

As migrations ficam em:

```text
src/main/resources/db/migration
```

| Versão | Estrutura criada                          |
|--------|-------------------------------------------|
| `V1`   | Categorias                                |
| `V2`   | Usuários                                  |
| `V3`   | Questões                                  |
| `V4`   | Alternativas                              |
| `V5`   | Tentativas de questão                     |
| `V6`   | Campo de ativação das alternativas        |

Uma migration aplicada não deve ser modificada. Mudanças futuras no banco devem
ser criadas em um novo arquivo versionado, por exemplo:

```text
V7__descricao_da_alteracao.sql
```

O Hibernate está configurado com `ddl-auto: validate`, portanto valida o schema
sem criar ou modificar tabelas.

## Perfis de acesso

A autenticação é stateless baseada em JWT. Os perfis disponíveis são:

| Perfil          | Permissões                                                              |
|-----------------|-------------------------------------------------------------------------|
| `ADMINISTRADOR` | Acesso total, incluindo criação, edição e desativação de conteúdo       |
| `ALUNO`         | Acesso de leitura a categorias, questões e alternativas; envio de respostas |

Rotas de modificação (POST, PUT, PATCH /desativar) de categorias, questões e
alternativas exigem o perfil `ADMINISTRADOR`. Requisições sem a permissão
adequada retornam `403 Forbidden`.

## CORS

O backend aceita requisições das seguintes origens durante o desenvolvimento:

| Origem                    | Framework              |
|---------------------------|------------------------|
| `http://localhost:3000`   | React (CRA)            |
| `http://localhost:5173`   | Vite (React/Vue)       |
| `http://localhost:4200`   | Angular                |
| `http://localhost:8081`   | Outros                 |

Para adicionar novas origens, edite `corsConfigurationSource()` em `SecurityConfig.java`.

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

### Autenticação

| Método | Endpoint          | Descrição                                   | Acesso   |
|--------|-------------------|---------------------------------------------|----------|
| `POST` | `/api/auth/login` | Realiza o login e retorna o token JWT       | Público  |

### Alunos

| Método | Endpoint                      | Descrição                                   | Acesso        |
|--------|-------------------------------|---------------------------------------------|---------------|
| `POST` | `/api/alunos`                 | Cadastra um novo aluno                      | Público       |
| `GET`  | `/api/alunos/me`              | Retorna os dados do aluno autenticado       | Autenticado   |
| `GET`  | `/api/alunos/me/desempenho`   | Retorna o desempenho do aluno por categoria | Autenticado   |

### Categorias

| Método  | Endpoint                          | Descrição                        | Acesso        |
|---------|-----------------------------------|----------------------------------|---------------|
| `POST`  | `/api/categorias`                 | Cadastra uma categoria           | **ADMIN**     |
| `GET`   | `/api/categorias`                 | Lista categorias ativas          | Autenticado   |
| `GET`   | `/api/categorias/{id}`            | Busca uma categoria pelo ID      | Autenticado   |
| `PUT`   | `/api/categorias/{id}`            | Atualiza uma categoria           | **ADMIN**     |
| `PATCH` | `/api/categorias/{id}/desativar`  | Desativa uma categoria           | **ADMIN**     |

### Questões

| Método  | Endpoint                        | Descrição                              | Acesso        |
|---------|---------------------------------|----------------------------------------|---------------|
| `POST`  | `/api/questoes`                 | Cadastra uma questão                   | **ADMIN**     |
| `GET`   | `/api/questoes`                 | Lista questões ativas                  | Autenticado   |
| `GET`   | `/api/questoes/estudo`          | Busca um lote de questões para estudo  | Autenticado   |
| `GET`   | `/api/questoes/{id}`            | Busca uma questão pelo ID              | Autenticado   |
| `PUT`   | `/api/questoes/{id}`            | Atualiza uma questão                   | **ADMIN**     |
| `PATCH` | `/api/questoes/{id}/desativar`  | Desativa uma questão                   | **ADMIN**     |

### Alternativas

| Método  | Endpoint                                                    | Descrição                        | Acesso        |
|---------|-------------------------------------------------------------|----------------------------------|---------------|
| `POST`  | `/api/questoes/{questaoId}/alternativas`                    | Cadastra uma alternativa         | **ADMIN**     |
| `GET`   | `/api/questoes/{questaoId}/alternativas`                    | Lista alternativas ativas        | Autenticado   |
| `GET`   | `/api/questoes/{questaoId}/alternativas/{id}`               | Busca uma alternativa pelo ID    | Autenticado   |
| `PUT`   | `/api/questoes/{questaoId}/alternativas/{id}`               | Atualiza uma alternativa         | **ADMIN**     |
| `PATCH` | `/api/questoes/{questaoId}/alternativas/{id}/desativar`     | Desativa uma alternativa         | **ADMIN**     |

### Respostas

| Método | Endpoint        | Descrição                                               | Acesso      |
|--------|-----------------|---------------------------------------------------------|-------------|
| `POST` | `/api/respostas`| Registra a tentativa de resposta e retorna a correção   | Autenticado |

### Ranking

| Método | Endpoint       | Descrição                               | Acesso      |
|--------|----------------|-----------------------------------------|-------------|
| `GET`  | `/api/ranking` | Retorna o ranking geral de pontuação    | Autenticado |

## Erros

As respostas de erro seguem um formato comum:

```json
{
  "mensagem": "descrição do erro"
}
```

| Status | Situação                                           |
|--------|----------------------------------------------------|
| `400`  | Dados de entrada inválidos (Bean Validation)       |
| `401`  | Token ausente, inválido ou expirado                |
| `403`  | Perfil sem permissão para a operação               |
| `404`  | Recurso não encontrado                             |
| `409`  | Conflito de dados (ex: e-mail já cadastrado)       |

# Cachly Backend

Backend da plataforma Cachly, responsável pela API REST, regras de negócio,
persistência, validações, simulação didática e integração com o PostgreSQL.

## Tecnologias

- Java 21 (LTS)
- Spring Boot 4.1
- Maven Wrapper
- Spring Security 6 (JWT stateless via cookies HttpOnly com SameSite=Strict)
- Spring Data JPA
- Bean Validation
- PostgreSQL 15/16
- Flyway (Migrations versionadas V1 a V14)
- SpringDoc OpenAPI (Swagger UI)
- JUnit 5, Mockito e testes de concorrência multithreaded

O pacote base da aplicação é:

```text
br.com.cachly.backend
```

## Pré-requisitos

Para executar o backend localmente, instale:

- Java 21 (JDK);
- PostgreSQL;
- Git;
- IntelliJ IDEA, VS Code ou outra IDE compatível com Maven e Java 21.

## Configuração do PostgreSQL

Crie um banco vazio chamado `cachly`:

```sql
CREATE DATABASE cachly;
```

As tabelas não precisam ser criadas manualmente. O Flyway executa as migrations
automaticamente quando o backend é iniciado.

### Variáveis de ambiente

| Variável              | Obrigatória | Valor padrão                              | Descrição |
|-----------------------|:-----------:|-------------------------------------------|-----------|
| `DB_URL`              | Não         | `jdbc:postgresql://localhost:5432/cachly` | URL JDBC de conexão ao PostgreSQL |
| `DB_USERNAME`         | Não         | `postgres`                                | Usuário do banco de dados |
| `DB_PASSWORD`         | **Sim**     | —                                         | Senha do banco de dados |
| `JWT_SECRET`          | **Sim**     | —                                         | Chave secreta HMAC para assinatura e validação do token JWT |
| `JWT_EXPIRATION_HOURS`| Não         | `24`                                      | Tempo de expiração padrão do token em horas |

> [!IMPORTANT]
> Credenciais e segredos (`DB_PASSWORD`, `JWT_SECRET`) nunca devem ser adicionados diretamente ao repositório Git.

Exemplo no PowerShell:

```powershell
$env:DB_PASSWORD="sua_senha"
$env:JWT_SECRET="segredo-de-desenvolvimento-local-do-cachly-com-tamanho-suficiente-32-chars"
```

Exemplo no Git Bash ou macOS/Linux:

```bash
export DB_PASSWORD="sua_senha"
export JWT_SECRET="segredo-de-desenvolvimento-local-do-cachly-com-tamanho-suficiente-32-chars"
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
de requisição e respostas esperadas.

## Executando os testes

A suíte de testes inclui testes unitários (Mockito) e testes de integração com banco de dados real PostgreSQL e cenários de concorrência com threads simultâneas.

No PowerShell ou Prompt de Comando:

```powershell
.\mvnw.cmd test
```

No Git Bash, Linux ou macOS:

```bash
./mvnw test
```

> [!NOTE]
> O banco de dados PostgreSQL deve estar em execução durante os testes de integração.

## Flyway Migrations

As migrations ficam em:

```text
src/main/resources/db/migration
```

| Versão | Estrutura criada / alterada |
|--------|-----------------------------|
| `V1`   | Criação da tabela de categorias |
| `V2`   | Criação da tabela de usuários |
| `V3`   | Criação da tabela de questões |
| `V4`   | Criação da tabela de alternativas |
| `V5`   | Criação da tabela de tentativas de questão |
| `V6`   | Campo de ativação (`ativa`) em alternativas |
| `V7`   | Colunas de ofensiva do aluno (`dias_ofensiva`, `data_ultima_ofensiva`) |
| `V8`   | Colunas de auditoria (`criado_em`, `atualizado_em`) |
| `V9`   | Coluna `xp_semanal` e índice para ranking/liga |
| `V10`  | Desativação de questões órfãs |
| `V11`  | Criação da tabela `ranking_historico` |
| `V12`  | Adição de `versao_token` em usuários (revogação instantânea de sessões) |
| `V13`  | Correção de tipo de identificador em histórico de ranking |
| `V14`  | Criação da tabela `desafios_concluidos` com unique constraint `(usuario_id, desafio_id)` |

O Hibernate está configurado com `ddl-auto: validate`, garantindo conformidade estrita entre as entidades JPA e o schema gerido pelo Flyway.

## Perfis de acesso e Segurança

A autenticação é stateless baseada em token JWT transmitido via cookie seguro `HttpOnly` com flag `SameSite=Strict`. Os perfis de acesso são:

| Perfil          | Descrição e Permissões |
|-----------------|------------------------|
| `ADMINISTRADOR` | Gestão de conteúdo pedagógico: criação, edição e desativação lógica de categorias e questões. |
| `ALUNO`         | Resolução de questões técnicas, submissão de desafios no simulador, consulta de desempenho, histórico e participação no ranking. |

- Rotas administrativas exigem `@PreAuthorize("hasRole('ADMINISTRADOR')")`.
- Rotas de submissão de respostas e desafios são exclusivas de estudantes: `@PreAuthorize("hasRole('ALUNO')")`.
- Tentativas de acesso sem permissão retornam `403 Forbidden`.

## Controle de Concorrência

Para evitar problemas de concorrência (como pontuação duplicada de XP ou corrupção de sessões transacionais), o backend adota:
- **Lock Pessimista Antecipado**: `findByIdForUpdate` no registro do usuário antes de avaliar duplicidades em submissão de respostas (`RespostaService`) e verificação de desafios (`DesafioCacheService`).
- **Idempotência**: Uma questão ou desafio resolvido corretamente mais de uma vez não gera concessão duplicada de XP nem erro 500 para o aluno.

## CORS

O backend aceita requisições das origens configuradas em `SecurityConfig.java` (ex: `localhost:4200`, `localhost:3000`, `localhost:5173`, `localhost:8081`). Quando executado via Docker Compose, o Nginx atua como proxy reverso roteando `/api/` internamente.

## Endpoints da API

### Health check

| Método | Endpoint      | Descrição           | Acesso  |
|--------|---------------|---------------------|---------|
| `GET`  | `/api/health` | Status da aplicação | Público |

### Autenticação (`/api/auth`)

| Método | Endpoint                    | Descrição                                                              | Acesso      |
|--------|-----------------------------|------------------------------------------------------------------------|-------------|
| `POST` | `/api/auth/login`           | Autentica usuário e define cookie `token` (`HttpOnly`, `SameSite=Strict`) | Público     |
| `POST` | `/api/auth/logout`          | Invalida o cookie de sessão                                           | Público     |
| `GET`  | `/api/auth/me`              | Retorna dados do usuário autenticado atual                             | Autenticado |
| `POST` | `/api/auth/alterar-senha`   | Altera senha e revoga tokens anteriores                                | Autenticado |
| `POST` | `/api/auth/revogar-sessoes` | Revoga todas as sessões ativas via incremento de `versaoToken`         | Autenticado |

### Alunos (`/api/alunos`)

| Método | Endpoint                    | Descrição                                             | Acesso      |
|--------|-----------------------------|-------------------------------------------------------|-------------|
| `POST` | `/api/alunos`               | Cadastra novo aluno (perfil fixado como `ALUNO`)       | Público     |
| `GET`  | `/api/alunos/me/desempenho` | Retorna estatísticas por categoria (radar pedagógico) | Autenticado |
| `GET`  | `/api/alunos/me/historico`  | Retorna histórico paginado de tentativas do aluno     | Autenticado |

### Categorias (`/api/categorias`)

| Método  | Endpoint                         | Descrição                         | Acesso        |
|---------|----------------------------------|-----------------------------------|---------------|
| `POST`  | `/api/categorias`                | Cadastra uma categoria            | **ADMIN**     |
| `GET`   | `/api/categorias`                | Lista categorias ativas           | Autenticado   |
| `GET`   | `/api/categorias/{id}`           | Busca categoria por ID            | Autenticado   |
| `PUT`   | `/api/categorias/{id}`           | Atualiza uma categoria            | **ADMIN**     |
| `PATCH` | `/api/categorias/{id}/desativar` | Desativação lógica de categoria   | **ADMIN**     |

### Questões (`/api/questoes`)

| Método  | Endpoint                       | Descrição                                                    | Acesso        |
|---------|--------------------------------|--------------------------------------------------------------|---------------|
| `POST`  | `/api/questoes`                | Cadastra questão e suas alternativas agregadas               | **ADMIN**     |
| `GET`   | `/api/questoes`                | Lista questões ativas com gabarito                           | **ADMIN**     |
| `GET`   | `/api/questoes/admin/todas`    | Lista todas as questões (incluindo inativas)                | **ADMIN**     |
| `GET`   | `/api/questoes/estudo`         | Lote de questões para estudo (sem exposição de gabarito)     | Autenticado   |
| `GET`   | `/api/questoes/{id}`           | Detalhes da questão por ID                                   | **ADMIN**     |
| `PUT`   | `/api/questoes/{id}`           | Atualiza questão e suas alternativas                         | **ADMIN**     |
| `PATCH` | `/api/questoes/{id}/desativar` | Desativação lógica de questão                                | **ADMIN**     |
| `POST`  | `/api/questoes/{id}/respostas` | Registra tentativa de resposta, calcula acerto e concede XP  | **ALUNO**     |

### Simulador e Desafios (`/api/simulador`)

| Método | Endpoint                             | Descrição                                                              | Acesso      |
|--------|--------------------------------------|------------------------------------------------------------------------|-------------|
| `POST` | `/api/simulador/executar`            | Simula acessos de cache e retorna snapshots detalhados                 | Autenticado |
| `GET`  | `/api/simulador/desafios`            | Lista desafios pedagógicos disponíveis no laboratório                  | Autenticado |
| `GET`  | `/api/simulador/desafios/{id}`       | Retorna dados e configuração de um desafio específico                  | Autenticado |
| `POST` | `/api/simulador/desafios/{id}/verificar` | Valida solução do desafio, roda simulação e pontua XP único       | **ALUNO**   |

### Ranking e Liga (`/api/ranking`)

| Método | Endpoint                         | Descrição                                              | Acesso      |
|--------|----------------------------------|--------------------------------------------------------|-------------|
| `GET`  | `/api/ranking`                   | Ranking paginado por XP semanal (Liga Semanal ativa)   | Autenticado |
| `GET`  | `/api/ranking/historico/datas`   | Lista datas de ligas semanais anteriores arquivadas    | Autenticado |
| `GET`  | `/api/ranking/historico`         | Consulta histórico de posições por data específica     | Autenticado |

## Tratamento Padronizado de Erros

As respostas de erro da API seguem o padrão padronizado definido por `ErroResponse`:

```json
{
  "momento": "2026-09-10T09:48:30.421-03:00",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Existem campos inválidos na requisição",
  "caminho": "/api/alunos",
  "campos": {
    "email": "O e-mail informado é inválido"
  }
}
```

| Código HTTP | Significado | Exemplo |
|:-----------:|-------------|---------|
| `400` | Bad Request | Falhas em validação de campos (`MethodArgumentNotValidException`, `RegraNegocioException`) |
| `401` | Unauthorized | Falha de autenticação ou token inexistente/expirado |
| `403` | Forbidden | Acesso negado pelo RBAC (ex: Aluno tentando rota de Admin) |
| `404` | Not Found | Recurso não encontrado ou inativo (`RecursoNaoEncontradoException`) |
| `409` | Conflict | Conflito de integridade ou duplicidade (`ConflitoDeDadosException`) |
| `500` | Internal Server Error | Erros inesperados não tratados (com log detalhado no servidor) |

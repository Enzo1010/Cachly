# Questly

O Questly é uma plataforma educacional gamificada para o estudo de Sistemas
Digitais e Arquitetura de Computadores. O objetivo é permitir que estudantes
pratiquem conteúdos por meio de questões de múltipla escolha, recebam a correção
das respostas e acompanhem seu desempenho e sua progressão.

O projeto está em desenvolvimento acadêmico por uma equipe de quatro pessoas:
duas responsáveis pelo frontend, uma pelo backend e uma pela documentação.

## Tecnologias

### Backend

- Java 21
- Spring Boot 4.1
- Maven
- Spring Data JPA
- Bean Validation
- PostgreSQL
- Flyway
- API REST

### Frontend

- Angular 21
- TypeScript
- npm
- Capacitor planejado para uma futura versão Android

## Estrutura do projeto

```text
Questly/
├── backend/    # API REST, regras de negócio e acesso ao banco
├── frontend/   # Aplicação Angular
└── README.md
```

O pacote base do backend é:

```text
br.com.questly.backend
```

## Pré-requisitos

Para executar o projeto localmente, instale:

- Git
- Java 21
- PostgreSQL
- Node.js e npm
- IntelliJ IDEA ou outra IDE compatível com Java e Maven

## Clonando o repositório

```bash
git clone https://git.uricer.edu.br/112963/Questly.git
cd Questly
```

## Configuração do PostgreSQL

Crie um banco vazio chamado `questly`:

```sql
CREATE DATABASE questly;
```

Não é necessário criar as tabelas manualmente. Ao iniciar o backend, o Flyway
executa as migrations e prepara o schema automaticamente.

O backend aceita as seguintes variáveis de ambiente:

| Variável | Valor padrão |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/questly` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `postgres` |

Exemplo para configurar somente a senha no PowerShell:

```powershell
$env:DB_PASSWORD="sua_senha"
```

No IntelliJ IDEA, essas variáveis podem ser adicionadas em **Run > Edit
Configurations > Environment variables**. Credenciais pessoais não devem ser
adicionadas ao Git.

## Executando o backend

No PowerShell ou Prompt de Comando do Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

No Git Bash, Linux ou macOS:

```bash
cd backend
./mvnw spring-boot:run
```

Para executar os testes no Windows:

```powershell
.\mvnw.cmd test
```

Com a aplicação iniciada, o health check estará disponível em:

```text
GET http://localhost:8080/api/health
```

Resposta esperada:

```json
{
  "status": "UP",
  "application": "Questly"
}
```

## Executando o frontend

```bash
cd frontend
npm install
npm start
```

Por padrão, a aplicação Angular ficará disponível em:

```text
http://localhost:4200
```

## Migrations do banco

As migrations ficam em:

```text
backend/src/main/resources/db/migration
```

Migrations existentes:

| Versão | Estrutura criada |
| --- | --- |
| `V1` | Categorias |
| `V2` | Usuários |
| `V3` | Questões |
| `V4` | Alternativas |
| `V5` | Tentativas de questão |

Uma migration que já foi aplicada não deve ser alterada. Mudanças futuras no
banco devem ser feitas em um novo arquivo versionado, como
`V6__descricao_da_alteracao.sql`.

## API disponível atualmente

### Categorias

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/api/categorias` | Cadastra uma categoria |
| `GET` | `/api/categorias` | Lista categorias ativas |
| `GET` | `/api/categorias/{id}` | Busca uma categoria pelo ID |
| `PUT` | `/api/categorias/{id}` | Atualiza uma categoria |
| `PATCH` | `/api/categorias/{id}/desativar` | Desativa uma categoria |

Exemplo de corpo para cadastro ou atualização:

```json
{
  "nome": "Álgebra Booleana",
  "descricao": "Questões sobre expressões e operações booleanas"
}
```

O backend utiliza respostas HTTP padronizadas para validação, recurso não
encontrado e conflito de dados. A autenticação ainda não foi implementada.

## Modelo inicial de dados

O banco possui inicialmente as tabelas:

- `usuarios`
- `categorias`
- `questoes`
- `alternativas`
- `tentativas_questao`

As tentativas registram a questão respondida, a alternativa escolhida, o
resultado e o XP concedido. As regras de repetição, progressão e competições
serão implementadas em etapas futuras.

## Próximas etapas do backend

1. Adicionar testes automatizados ao módulo de categorias.
2. Implementar o gerenciamento de questões e alternativas.
3. Implementar cadastro de alunos e autenticação.
4. Registrar respostas e calcular XP.
5. Calcular desempenho e progressão.
6. Implementar ranking e ligas semanais.

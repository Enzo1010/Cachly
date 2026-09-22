# Guia de Execução

Este guia concentra as instruções de como configurar, rodar e testar o Cachly nos ambientes local e Docker.

## Como Executar o Projeto via Docker Compose (Recomendada)

Com o Docker e Docker Desktop instalados, você pode subir o banco de dados, o backend e o frontend com um único comando:

1. **Configurar variáveis de ambiente**:
   Crie o arquivo `.env` na raiz a partir do modelo de exemplo:
   ```bash
   cp .env.example .env
   ```
   *Edite o arquivo `.env` gerado definindo uma senha para o banco de dados e um segredo forte para o JWT (mínimo de 32 caracteres).*

2. **Iniciar os containers**:
   ```bash
   docker compose up --build
   ```
   *(Adicione a flag `-d` para rodar em segundo plano desanexado do terminal).*

3. **Acessar os serviços**:
   - **Frontend (Aplicação Web):** [http://localhost](http://localhost)
   - **Backend (API REST / Swagger):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - **Banco de Dados (PostgreSQL):** `localhost:5432` (bd: `cachly`, usuário: `postgres`)

4. **Derrubar os containers**:
   ```bash
   docker compose down
   ```
   *Para apagar também os volumes locais do banco de dados:* `docker compose down -v`.

---

## Execução Local Tradicional (Sem Docker)

### 1. Banco de Dados
Certifique-se de que o PostgreSQL esteja em execução na porta `5432`.
Crie um banco vazio chamado `cachly`:
```sql
CREATE DATABASE cachly;
```
As tabelas não precisam ser criadas manualmente. O Flyway executa as migrations automaticamente quando o backend é iniciado.

### 2. Backend

**Pré-requisitos:** Java 21 e PostgreSQL.

Entre na pasta `backend`. Configure as variáveis de ambiente necessárias:
- `DB_PASSWORD`: Senha do seu PostgreSQL local.
- `JWT_SECRET`: Chave secreta de no mínimo 32 caracteres.

*(Exemplo no Windows PowerShell)*:
```powershell
$env:DB_PASSWORD="sua_senha"
$env:JWT_SECRET="segredo-de-desenvolvimento-local-do-cachly-com-tamanho-suficiente-32-chars"
.\mvnw.cmd spring-boot:run
```

*(Exemplo no Git Bash, Linux ou macOS)*:
```bash
export DB_PASSWORD="sua_senha"
export JWT_SECRET="segredo-de-desenvolvimento-local-do-cachly-com-tamanho-suficiente-32-chars"
./mvnw spring-boot:run
```
A API ficará disponível em `http://localhost:8080`.

**Para rodar os testes do backend:**
- `.\mvnw.cmd test` (Windows)
- `./mvnw test` (Linux/Mac)
*(O banco de dados PostgreSQL deve estar em execução durante os testes de integração)*

### 3. Frontend

**Pré-requisitos:** Node.js (compatível com Angular 21) e npm.

Entre na pasta `frontend/`:

1. **Instale as dependências:**
   ```bash
   npm install
   ```

2. **Inicie o servidor de desenvolvimento:**
   ```bash
   npm start
   ```
   Acesse em: [http://localhost:4200](http://localhost:4200) (as chamadas `/api` são redirecionadas via `proxy.conf.json`).

**Para rodar os testes do frontend:**
```bash
npm test -- --watch=false
```

**Para build de produção:**
```bash
npm run build
```
Os arquivos gerados serão armazenados em `dist/cachly-frontend/`.

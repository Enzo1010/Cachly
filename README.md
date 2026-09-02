# Cachly - Ambiente de Desenvolvimento com Docker

Este projeto está configurado para rodar localmente utilizando o Docker Compose, englobando o **Backend** (Spring Boot), **Frontend** (Angular) e **Banco de Dados** (PostgreSQL).

## Pré-requisitos
- [Docker](https://www.docker.com/products/docker-desktop/) instalado.

## Como rodar

1. Antes de executar pela primeira vez, copie o arquivo de configuração de exemplo e preencha os valores (o sistema não subirá sem a senha do banco e o segredo do JWT):
   ```bash
   cp .env.example .env
   ```
   *Edite o arquivo `.env` gerado com os valores desejados (nunca commite esse arquivo).*

2. Na raiz do projeto, execute o comando:
   ```bash
   docker compose up --build
   ```
   *Dica: Para rodar em background (desacoplado do terminal), adicione a flag `-d` (`docker compose up --build -d`).*

2. Acessando a aplicação:
   - **Frontend (SPA):** http://localhost
   - **Backend (API):** http://localhost:8080
   - **Banco de Dados:** A porta `5432` está exposta no localhost (usuário: `postgres`, senha: `postgres`, bd: `cachly`).

## Como funciona a comunicação (Proxy)
O frontend acessa o backend via caminho `/api/` no próprio `localhost:80`. O Nginx dentro do container do frontend faz um proxy reverso e encaminha as requisições para `http://backend:8080/api/` usando a rede interna do Docker. Isso evita problemas com CORS no navegador.

## Como derrubar os containers
Para parar e remover os containers gerados pelo projeto, execute:
```bash
docker compose down
```
Se quiser resetar também o banco de dados e limpar o volume armazenado localmente:
```bash
docker compose down -v
```

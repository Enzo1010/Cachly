<div align="center">
  <img src="frontend/public/cachly-logo-sem-fundo.png" alt="Cachly" width="280"/>

  <br/>

  [![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://adoptium.net/)
  [![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?style=flat-square&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
  [![Angular](https://img.shields.io/badge/Angular-21-DD0031?style=flat-square&logo=angular&logoColor=white)](https://angular.dev/)
  [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/download/)
  [![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)
  [![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)
  ![Tests](https://img.shields.io/badge/Tests-123_passing-brightgreen?style=flat-square)

  <br/>

  **Plataforma educacional gamificada para o estudo de Arquitetura de Computadores e Sistemas Digitais.**

</div>

---

## Contexto Acadêmico

Este projeto é desenvolvido como Trabalho de Projeto Integrador IV no âmbito da disciplina de **Projeto Integrador** da **URI Campus Erechim**.

**Tema Norteador**
> Cachly: Uma Plataforma Educacional para Aprendizagem e Acompanhamento do Desempenho em Sistemas Digitais e Arquitetura de Computadores

**Equipe de Desenvolvimento**

| Nome | Matrícula | E-mail |
|---|---|---|
| Enzo Bazzi de Oliveira | 112963 | [112963@aluno.uricer.edu.br](mailto:112963@aluno.uricer.edu.br) |
| Ezequiel Henrique Gazolla Muller | 111624 | [111624@aluno.uricer.edu.br](mailto:111624@aluno.uricer.edu.br) |
| Luis Fernando Refatti Boff | 103436 | [103436@aluno.uricer.edu.br](mailto:103436@aluno.uricer.edu.br) |
| Vitor Luis Andreolla | 111246 | [111246@aluno.uricer.edu.br](mailto:111246@aluno.uricer.edu.br) |

**Escopo de Pesquisa**

O estudo investiga o uso de plataformas educacionais aplicadas ao ensino de Sistemas Digitais e Arquitetura de Computadores, com foco em três eixos principais:

1. **Facilitação do aprendizado ativo** — resolução de questões técnicas organizadas por categoria e dificuldade, com correção em tempo real e proteção estrita de gabarito no backend.
2. **Rastreamento de desempenho e remediação ativa** — coleta de acertos, erros e mapeamento de fraquezas por conceito (gráfico de radar), sugerindo intervenções pedagógicas direcionadas ao laboratório.
3. **Simulação didática de hardware** — módulo de simulação de memória cache onde o estudante visualiza a decomposição binária, o comportamento passo a passo das linhas/conjuntos e compreende acertos (*hits*), faltas (*misses*) e políticas de substituição.

---

## Visão Geral

O **Cachly** é uma aplicação web full-stack projetada para tornar o aprendizado de hardware e sistemas digitais substancialmente mais efetivo. A plataforma combina um banco de questões técnicas estruturado por tópico e dificuldade com módulos de simulação interativos, entregando *feedback* imediato e progressão mensurável ao estudante.

O núcleo da proposta é a convergência entre **rigor técnico** e **design de engajamento**: o mesmo conteúdo que seria apresentado de forma árida em um livro-texto é assimilado por meio de tentativa, erro, explicação contextualizada e progressão gamificada.

## Funcionalidades

| Módulo | Descrição |
|---|---|
| **Simulador de Cache** | Laboratório interativo stateless para simulação de mapeamento Direto, Associativo por Conjunto ($N$-vias) e Totalmente Associativo com políticas LRU e FIFO. |
| **Guia Conceitual & Breakdown Binário** | Modal didático integrado com 4 abas teóricas (Aritmética, Mapeamentos, Substituição e Hit/Miss) e blocos interativos clicáveis de Tag, Índice e Offset. |
| **Banco de Questões & Estudo** | Questões técnicas categorizadas com proteção contra vazamento de respostas (`/api/questoes/estudo`), cálculo de pontuação por dificuldade e feedback imediato. |
| **Engine de Gamificação** | Sistema de XP com cálculo de níveis, ofensivas diárias (*streaks*) sob consistência transacional com Lock Pessimista e Liga Semanal com agendador (`RankingScheduler`). |
| **Análise de Desempenho** | Dashboard com gráfico de radar de domínio conceitual, identificação da categoria de menor rendimento e intervenção pedagógica que guia o aluno para o simulador. |
| **Segurança e Sessões** | Autenticação JWT via cookies seguros `HttpOnly` + `SameSite=Strict`, proteção nativa contra CSRF e revogação imediata de sessões via controle de versão de token (`versaoToken`). |

---

## Arquitetura

O sistema segue um modelo **Cliente-Servidor desacoplado**. A API REST e a SPA Angular são desenvolvidas de forma independente, comunicando-se via JSON com suporte a orquestração integrada via Docker Compose e proxy reverso Nginx.

```
┌───────────────────────────────────────────────────────────────────┐
│  Cliente (Navegador)                                              │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │  Angular 21 SPA                                             │  │
│  │  PrimeNG · Chart.js · Angular Signals · SCSS                │  │
│  └────────────────────────┬────────────────────────────────────┘  │
│                           │ HTTP / JSON (:80)                     │
└───────────────────────────┼───────────────────────────────────────┘
                            │
┌───────────────────────────┼───────────────────────────────────────┐
│  Nginx (Proxy Reverso)    │                                       │
│  Encaminha /api/ diretamente para o backend (elimina CORS)        │
└───────────────────────────┼───────────────────────────────────────┘
                            │
┌───────────────────────────┼───────────────────────────────────────┐
│  Servidor Backend         │                                       │
│                           ▼                                       │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │  Spring Boot 4.1 (Java 21)                                  │  │
│  │                                                             │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │  │
│  │  │  Security   │  │  Business   │  │  Simulator Engine   │  │  │
│  │  │  JWT Cookie │  │  Layer      │  │  (Stateless)        │  │  │
│  │  │  RBAC       │  │  Gamificação│  │  Bitwise Processing │  │  │
│  │  └─────────────┘  └──────┬──────┘  └─────────────────────┘  │  │
│  │                          │                                  │  │
│  │  ┌───────────────────────▼──────────────────────────────┐   │  │
│  │  │  Spring Data JPA  ·  Flyway Migrations (V1 → V7)     │   │  │
│  └──┴──────────────────────────────────────────────────────┴───┘  │
│                            │                                      │
│  ┌─────────────────────────▼───────────────────┐                  │
│  │  PostgreSQL 15 / 16                         │                  │
│  └─────────────────────────────────────────────┘                  │
└───────────────────────────────────────────────────────────────────┘
```

### Decisões de Design e Arquitetura (ADRs)

- **Autenticação via Cookies Seguros (`HttpOnly` + `SameSite=Strict`)**: Documentada na [ADR-001](docs/decisions/ADR-001-autenticacao-samesite-cookies.md). O JWT reside em cookie com flag `HttpOnly`, tornando-o inacessível via JavaScript (imunidade a XSS) e `SameSite=Strict`, impedindo envio em contextos cruzados (proteção robusta contra CSRF).
- **Revogação Instantânea Stateless (`versaoToken`)**: Documentada na [ADR-002](docs/decisions/ADR-002-revogacao-jwt-versao-token.md). Resolve a limitação de expiração passiva do JWT: ao alterar a senha ou revogar sessões, a coluna `versao_token` na entidade `Usuario` é incrementada. Tokens anteriores com claim `iat` desatualizada são rejeitados imediatamente no `SecurityFilter`.
- **Processamento do Simulador via Snapshots**: A engine do simulador de cache processa cada passo da sequência de acessos à memória no backend usando operadores bitwise nativos (`>>>`, `&`, `<<`) e devolve ao frontend um array imutável de deltas e explicações didáticas.
- **Controle de Concorrência por Lock Pessimista**: A atualização do XP e ofensiva do usuário usa `SELECT ... FOR UPDATE` (via `@Lock(LockModeType.PESSIMISTIC_WRITE)` do JPA), garantindo serialização transacional e eliminando *race conditions* em requisições concorrentes.
- **Integridade de Dados por Exclusão Lógica**: Questões e alternativas nunca são removidas fisicamente. A flag `ativa = false` preserva o histórico de tentativas e métricas retroativas.

---

## Stack Tecnológica

### Backend
| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem principal (LTS) |
| Spring Boot | 4.1 | Framework base da aplicação |
| Spring Security | 6.x | Autenticação JWT, filtros e RBAC |
| Spring Data JPA | 3.x | Camada de persistência relacional |
| PostgreSQL | 15 / 16 | Banco de dados relacional |
| Flyway | 10.x | Versionamento e migração de schema (V1 a V7) |
| SpringDoc OpenAPI | 2.x | Documentação interativa (Swagger UI) |
| JUnit 5 + Mockito | — | Cobertura de testes automatizados (99 casos) |
| Lombok | — | Redução de boilerplate |

### Frontend
| Tecnologia | Versão | Uso |
|---|---|---|
| Angular | 21 | Framework SPA moderno |
| TypeScript | 5.x | Linguagem base tipada |
| PrimeNG | 21.x | Biblioteca de componentes de interface rica |
| Chart.js | 4.x | Visualização gráfica (Hit/Miss e tendências) |
| Angular Signals | — | Gerenciamento reativo de estado de alta performance |
| SCSS | — | Estilização modular com design system proprietário |
| Vitest | 4.x | Suíte de testes unitários ultrarrápida (24 casos) |

### Infraestrutura & DevOps
| Ferramenta | Uso |
|---|---|
| **Docker** | Containerização isolada de cada serviço |
| **Docker Compose** | Orquestração local de múltiplos containers em rede interna |
| **Nginx** | Servidor web para a SPA e proxy reverso para a API REST |

---

## Como Executar o Projeto

### Opção 1: Via Docker Compose (Recomendada)

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

### Opção 2: Execução Local Tradicional (Sem Docker)

#### 1. Banco de Dados
Certifique-se de que o PostgreSQL esteja em execução na porta `5432` com a base `cachly` criada.

#### 2. Backend
No diretório `backend/`:
```bash
# Definir as variáveis de ambiente necessárias e iniciar
export JWT_SECRET="segredo-de-desenvolvimento-local-do-cachly-com-tamanho-suficiente"
export DB_PASSWORD="sua-senha-do-banco-aqui"
./mvnw spring-boot:run
```
*(No Windows PowerShell: `$env:JWT_SECRET="seu-segredo..."; $env:DB_PASSWORD="sua-senha..."; .\mvnw.cmd spring-boot:run`)*

#### 3. Frontend
No diretório `frontend/`:
```bash
npm install
npm start
```
Acesse em: [http://localhost:4200](http://localhost:4200) (as chamadas `/api` são redirecionadas via `proxy.conf.json`).

---

## Estrutura do Repositório

```
Cachly/
├── .dockerignore               # Arquivos ignorados no contexto Docker
├── .env.example                # Modelo de variáveis de ambiente para Docker
├── docker-compose.yml          # Orquestração dos containers (DB, API, Web)
│
├── backend/                    # API REST — Java / Spring Boot
│   ├── Dockerfile              # Imagem multi-stage do backend
│   ├── src/main/java/
│   │   └── br/com/cachly/backend/
│   │       ├── alternativa/    # Modelagem e repositório de alternativas
│   │       ├── categoria/      # Categorias de arquitetura e sistemas digitais
│   │       ├── comum/          # Exceções, DTOs globais e SecurityFilter
│   │       ├── questao/        # Endpoints protegidos e regras de questões
│   │       ├── resposta/       # Engine de respostas, XP e ofensivas
│   │       ├── simulador/      # Engine stateless de simulação de cache
│   │       └── usuario/        # Autenticação, alunos, perfis e ranking
│   ├── src/main/resources/
│   │   ├── db/migration/       # Scripts Flyway versionados (V1 → V7)
│   │   └── application.yml     # Configurações de banco, JWT e segurança
│   └── README.md
│
├── frontend/                   # SPA — Angular 21
│   ├── Dockerfile              # Imagem multi-stage do frontend
│   ├── nginx.conf              # Configuração do Nginx e proxy reverso /api/
│   ├── src/app/
│   │   ├── core/               # Guards de rota, interceptors, layout e sessão
│   │   ├── features/           # Módulos: simulador, estudar, desempenho, liga, perfil
│   │   └── shared/             # Modelos, componentes e utilitários globais
│   └── README.md
│
├── docs/
│   └── decisions/              # Architecture Decision Records (ADR-001, ADR-002)
└── README.md
```

---

## Qualidade e Cobertura de Testes

A suíte de testes cobre os fluxos críticos de negócio com testes unitários e de integração no backend (Mockito, MockMvc) e testes unitários de componentes e serviços no frontend (Vitest).

| Escopo | Tecnologia | Casos de Teste | Status |
|---|---|:---:|:---:|
| **Backend — Regras de Negócio, Gamificação e XP** | JUnit 5 + Mockito | 99 | 🟢 Passing |
| **Backend — Segurança JWT, Revogação e Integração** | MockMvc + Spring Profile `test` | Incluído | 🟢 Passing |
| **Frontend — Componentes, Estados e Serviços** | Vitest + Angular TestBed | 24 | 🟢 Passing |
| **Total de Casos Automatizados** | — | **123** | **0 Failures** |

Para executar as suítes completas:

```bash
# Testes do Backend
cd backend && ./mvnw test

# Testes do Frontend
cd frontend && npm test -- --watch=false
```

---

## Licença

Distribuído sob a licença MIT. Consulte o arquivo `LICENSE` para detalhes.
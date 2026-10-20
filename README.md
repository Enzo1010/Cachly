<div align="center">
  <img src="frontend/public/cachly-logo-sem-fundo.png" alt="Cachly" width="280"/>

  <br/>

  [![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://adoptium.net/)
  [![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?style=flat-square&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
  [![Angular](https://img.shields.io/badge/Angular-21-DD0031?style=flat-square&logo=angular&logoColor=white)](https://angular.dev/)
  [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/download/)
  [![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)
  ![Tests](https://img.shields.io/badge/Tests-automated-blue?style=flat-square)

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
3. **Simulação didática de hardware** — módulo de simulação de memória cache onde o estudante visualiza a decomposição binária, o comportamento passo a passo das linhas/conjuntos e compreende acertos (*hits*), faltas (*misses*), políticas de substituição e desafios práticos.

---

## Visão Geral

O **Cachly** é uma aplicação web full-stack projetada para tornar o aprendizado de hardware e sistemas digitais substancialmente mais efetivo. A plataforma combina um banco de questões técnicas estruturado por tópico e dificuldade com módulos de simulação interativos e desafios com gamificação integrada, entregando *feedback* imediato e progressão mensurável ao estudante.

O núcleo da proposta é a convergência entre **rigor técnico** e **design de engajamento**: o mesmo conteúdo que seria apresentado de forma árida em um livro-texto é assimilado por meio de tentativa, erro, explicação contextualizada e progressão gamificada.

## Funcionalidades

| Módulo | Descrição |
|---|---|
| **Simulador de Cache** | Laboratório interativo stateless para simulação de mapeamento Direto, Associativo por Conjunto ($N$-vias) e Totalmente Associativo com políticas LRU e FIFO. |
| **Laboratório de Desafios** | Cenários práticos guiados (Cold Miss, Thrashing, Associatividade, Políticas) com verificação em tempo real, proteção transacional e concessão de XP único. |
| **Painel de Gestão de Desafios (Admin)** | CRUD completo para administradores cadastrarem, editarem e gerenciarem desafios pedagógicos para o simulador. |
| **Guia Conceitual & Breakdown Binário** | Modal didático integrado com 4 abas teóricas (Aritmética, Mapeamentos, Substituição e Hit/Miss) e blocos interativos clicáveis de Tag, Índice e Offset. |
| **Banco de Questões & Estudo** | Questões técnicas categorizadas com proteção contra vazamento de respostas (`/api/questoes/estudo`), cálculo de pontuação por dificuldade e feedback imediato. |
| **Fórum de Dúvidas nas Questões** | Discussão colaborativa por questão, permitindo que estudantes publiquem dúvidas conceituais e atribuam upvotes para destacar as melhores respostas. |
| **Recuperação de Senha por E-mail** | Fluxo seguro de redefinição de credenciais via token temporário com envio transacional via Google Apps Script. |
| **Engine de Gamificação** | Sistema de XP com cálculo de níveis, ofensivas diárias (*streaks*) sob consistência transacional com Lock Pessimista antecipado e Liga Semanal com agendador (`RankingScheduler`). |
| **Análise de Desempenho** | Dashboard com gráfico de radar de domínio conceitual, identificação da categoria de menor rendimento e intervenção pedagógica que guia o aluno para o simulador. |
| **Segurança e Sessões** | Autenticação JWT via cookie `HttpOnly` + `Secure` + `SameSite=Strict`, RBAC para `ALUNO` e `ADMINISTRADOR` e revogação de tokens por `versaoToken`. O CSRF do Spring está desabilitado; a mitigação atual depende de a aplicação permanecer no mesmo site e do atributo `SameSite=Strict`. |

> **Nota sobre o Escopo Atual:** A **Liga Semanal** possui endpoints e agendador no backend, mas sua rota está desativada na interface. **Conquistas** possui somente uma tela desativada com dados mockados; não há endpoint ou persistência de conquistas no backend.

---

## Arquitetura

O sistema segue um modelo **Cliente-Servidor desacoplado**. A API REST e a SPA Angular são desenvolvidas de forma independente, comunicando-se via JSON com suporte a orquestração integrada via Docker Compose e proxy reverso Nginx.

```text
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
│  │  │  Spring Data JPA  ·  Flyway Migrations (V1 → V19)    │   │  │
│  └──┴──────────────────────────────────────────────────────┴───┘  │
│                            │                                      │
│  ┌─────────────────────────▼───────────────────┐                  │
│  │  PostgreSQL 15 (Docker Compose)             │                  │
│  └─────────────────────────────────────────────┘                  │
└───────────────────────────────────────────────────────────────────┘
```

### Documentação de Arquitetura e Engenharia

- **[Diagrama de Classes (Modelo de Domínio)](docs/diagrama_de_classes/DIAGRAMA_CLASSES.md)**: Mapeamento de todas as entidades JPA, relacionamentos e controle de auditoria de ponta a ponta.

### Decisões de Design e Arquitetura (ADRs)

- **Autenticação via Cookies Seguros (`HttpOnly` + `SameSite=Strict`)**: Documentada na [ADR-001](docs/decisions/ADR-001-autenticacao-samesite-cookies.md). O JWT não pode ser lido por JavaScript por causa de `HttpOnly`; isso reduz o impacto de roubo de token por XSS, mas não elimina o risco de XSS. `SameSite=Strict` é a mitigação de CSRF adotada enquanto frontend e API permanecem no mesmo site.
- **Revogação de Sessões por `versaoToken`**: Documentada na [ADR-002](docs/decisions/ADR-002-revogacao-jwt-versao-token.md). Ao alterar a senha ou revogar sessões, a coluna `versao_token` é atualizada. Tokens anteriores são recusados pelo `SecurityFilter`, com tolerância deliberada de até um segundo para a precisão temporal da claim `iat`.
- **Processamento do Simulador via Snapshots**: A engine do simulador de cache processa cada passo da sequência de acessos à memória no backend usando operadores bitwise nativos (`>>>`, `&`, `<<`) e devolve ao frontend um array imutável de deltas e explicações didáticas.
- **Controle de Concorrência por Lock Pessimista Antecipado**: A verificação de duplicidade e atualização do XP/ofensiva do usuário utiliza `SELECT ... FOR UPDATE` (via `@Lock(LockModeType.PESSIMISTIC_WRITE)` do JPA) antecipado no início das transações de resposta e desafios, eliminando *race conditions* e prevenindo erros de concorrência ou duplicação de pontuação.
- **Integridade de Dados por Exclusão Lógica**: No fluxo administrativo, questões, categorias e alternativas usam a flag `ativa = false`, preservando o histórico de tentativas e métricas retroativas. O seeder de demonstração preenche apenas bancos vazios e preserva os dados existentes em reinicializações e deploys.

---

## Stack Tecnológica

### Backend
| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem principal (LTS) |
| Spring Boot | 4.1 | Framework base da aplicação |
| Spring Security | 7.1 | Autenticação JWT via cookie HttpOnly, filtros e RBAC |
| Spring Data JPA | 4.1 | Camada de persistência relacional |
| PostgreSQL | 15 | Banco usado pelo Docker Compose |
| Flyway | 12.4 | Versionamento e migração de schema (V1 a V19) |
| SpringDoc OpenAPI | 3.1 | Documentação interativa (Swagger UI) |
| JUnit 5 + Mockito | — | Testes automatizados de unidade, MVC, integração e concorrência |
| Lombok | — | Redução de boilerplate |

### Frontend
| Tecnologia | Versão | Uso |
|---|---|---|
| Angular | 21 | Framework SPA moderno |
| TypeScript | 5.x | Linguagem base tipada |
| PrimeNG | 21.x | Biblioteca de componentes de interface rica |
| Chart.js | 4.x | Visualização gráfica (Hit/Miss e tendências) |
| Angular Signals | 21 | Gerenciamento reativo de estado de alta performance |
| SCSS | — | Estilização modular com design system proprietário |
| Vitest | 4.x | Suíte de testes unitários ultrarrápida (56 casos) |

### Infraestrutura & DevOps
| Ferramenta | Uso |
|---|---|
| **Docker** | Containerização isolada de cada serviço |
| **Docker Compose** | Orquestração local de múltiplos containers em rede interna |
| **Nginx** | Servidor web para a SPA e proxy reverso para a API REST |

---

## Como Executar o Projeto

Para visualizar as instruções detalhadas de como configurar variáveis de ambiente, executar o projeto via Docker Compose (recomendado) ou rodar o backend e frontend localmente, consulte a documentação dedicada:

**[Guia de Execução (RUNNING.md)](docs/RUNNING.md)**

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
│   │       ├── questao/        # Endpoints protegidos, regras de questões e comentários
│   │       ├── resposta/       # Engine de respostas, XP e ofensivas
│   │       ├── simulador/      # Engine stateless de simulação de cache e gestão de desafios
│   │       └── usuario/        # Autenticação, alunos, perfis e ranking
│   ├── src/main/resources/
│   │   ├── db/migration/       # Scripts Flyway versionados (V1 → V19)
│   │   └── application.yml     # Configurações de banco, JWT e segurança
│   └── README.md
│
├── frontend/                   # SPA — Angular 21
│   ├── Dockerfile              # Imagem multi-stage do frontend
│   ├── nginx.conf              # Configuração do Nginx e proxy reverso /api/
│   ├── src/app/
│   │   ├── core/               # Guards de rota, interceptors, layout e sessão
│   │   ├── features/           # Módulos: simulador, estudar, desempenho, liga, perfil, admin
│   │   └── shared/             # Modelos, componentes e utilitários globais
│   └── README.md
│
├── docs/
│   ├── decisions/              # Architecture Decision Records (ADR-001, ADR-002)
│   ├── diagrama_de_classes/    # Diagrama de Classes UML do domínio
│   └── RUNNING.md              # Guia consolidado de execução
└── README.md
```

---

## Qualidade e Cobertura de Testes

A suíte combina testes unitários, MVC, integração e concorrência no backend, além de testes unitários de componentes e serviços no frontend. O resultado deve ser verificado no ambiente em que o projeto será executado; o backend depende de PostgreSQL nos testes de integração e inicializa o cliente HTTP do serviço de e-mail durante o contexto Spring.

Para executar as suítes completas:

```bash
# Testes do Backend
cd backend && ./mvnw test

# Testes do Frontend
cd frontend && npm test -- --watch=false
```

---

## Propriedade e Autoria

Este projeto é de propriedade exclusiva de seus autores, desenvolvido como 
Projeto Integrador IV do curso de Ciência da Computação — Universidade Regional 
Integrada do Alto Uruguai e das Missões (URI), Câmpus Erechim.

**Autores:**
- Enzo Bazzi de Oliveira
- Ezequiel Henrique Gazolla Muller
- Luis Fernando Refatti Boff
- Vitor Luis Andreolla

**Orientador:** Hercio Menegotto Ferraro Neto

Todos os direitos reservados. A reprodução, distribuição ou uso deste código sem 
autorização expressa dos autores não é permitida.

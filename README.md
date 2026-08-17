<div align="center">
  <img src="frontend/public/cachly-logo-sem-fundo.png" alt="Cachly" width="280"/>

  <br/>

  [![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://adoptium.net/)
  [![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?style=flat-square&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
  [![Angular](https://img.shields.io/badge/Angular-21-DD0031?style=flat-square&logo=angular&logoColor=white)](https://angular.dev/)
  [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/download/)
  [![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)
  ![Tests](https://img.shields.io/badge/Tests-115_passing-brightgreen?style=flat-square)

  <br/>

  **Plataforma educacional gamificada para o estudo de Arquitetura de Computadores e Sistemas Digitais.**

</div>

---

## Contexto Acadêmico

Este projeto é desenvolvido como Trabalho de Conclusão de Curso (TCC) no âmbito da disciplina de **Sistemas Digitais e Arquitetura de Computadores** da **URI Campus Erechim (URICER)**.

**Tema Norteador**
> Cachly: Uma Plataforma Educacional para Aprendizagem e Acompanhamento do Desempenho em Sistemas Digitais e Arquitetura de Computadores

**Equipe de Desenvolvimento**

| Nome | Matrícula |
|---|---|
| Enzo Bazzi de Oliveira | [112963@aluno.uricer.edu.br](mailto:112963@aluno.uricer.edu.br) |
| Ezequiel Henrique Gazolla Muller | [111624@aluno.uricer.edu.br](mailto:111624@aluno.uricer.edu.br) |
| Luis Fernando Refatti Boff | [103436@aluno.uricer.edu.br](mailto:103436@aluno.uricer.edu.br) |
| Vitor Luis Andreolla | [111246@aluno.uricer.edu.br](mailto:111246@aluno.uricer.edu.br) |

**Escopo de Pesquisa**

O estudo investiga o uso de plataformas educacionais aplicadas ao ensino de Sistemas Digitais e Arquitetura de Computadores, com foco em três eixos principais:

1. **Facilitação do aprendizado ativo** — resolução de questões organizadas por categoria e nível de dificuldade, com correção e explicação técnica imediata.
2. **Rastreamento de desempenho** — coleta sistemática de acertos, erros e estatísticas por tópico, permitindo ao estudante identificar com precisão as áreas que demandam maior dedicação.
3. **Simulação didática de hardware** — módulo de simulação de memória cache onde o estudante fornece uma sequência de acessos à memória e visualiza, passo a passo, o comportamento interno da cache durante a execução.

---

## Visão Geral

O **Cachly** é uma aplicação web full-stack projetada para tornar o aprendizado de hardware e sistemas digitais substancialmente mais efetivo. A plataforma combina um banco de questões técnicas estruturado por tópico e dificuldade com módulos de simulação interativos, entregando *feedback* imediato e progressão mensurável ao estudante.

O núcleo da proposta é a convergência entre **rigor técnico** e **design de engajamento**: o mesmo conteúdo que seria apresentado de forma árida em um livro-texto é assimilado por meio de tentativa, erro, explicação contextualizada e progressão gamificada.

## Funcionalidades

| Módulo | Descrição |
|---|---|
| **Banco de Questões** | Questões de múltipla escolha por categoria (CPU, Cache, Álgebra Booleana, Pipeline, etc.) e nível de dificuldade, com correção e explicação técnica imediata. |
| **Engine de Gamificação** | Sistema de XP com fórmula de progressão quadrática, níveis nomeados, ofensivas diárias (*streaks*) e ranking global baseado em experiência acumulada. |
| **Simulador de Cache** | Laboratório interativo stateless para simulação de políticas de mapeamento (Direto, Conjunto Associativo e Totalmente Associativo) com LRU e FIFO. |
| **Painel Didático Bitwise** | Decomposição visual em tempo real de endereços de memória em Tag, Índice e Offset com código de cores, reforçando a compreensão da aritmética binária. |
| **Análise de Desempenho** | Dashboard com taxa de acerto por categoria, histórico de tentativas e gráficos de Hit/Miss ratio por sessão de simulação. |
| **Gerenciamento de Conteúdo** | Interface administrativa para criação e manutenção de categorias, questões e alternativas, com exclusão lógica para preservação do histórico. |

## Arquitetura

O sistema segue um modelo **Cliente-Servidor desacoplado**. A API REST e o SPA Angular são desenvolvidos e implantados de forma independente, comunicando-se exclusivamente via JSON.

```
┌───────────────────────────────────────────────────────────────────┐
│  Cliente (Browser)                                                │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │  Angular 21 SPA                                             │  │
│  │  PrimeNG · Chart.js · Angular Signals · SCSS               │  │
│  └────────────────────────┬────────────────────────────────────┘  │
│                           │ HTTPS / JSON                          │
└───────────────────────────┼───────────────────────────────────────┘
                            │
┌───────────────────────────┼───────────────────────────────────────┐
│  Servidor                 │                                       │
│                           ▼                                       │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │  Spring Boot 4.1 (Java 21)                                  │  │
│  │                                                             │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │  │
│  │  │  Security   │  │  Business   │  │  Simulator Engine   │ │  │
│  │  │  JWT Filter │  │  Layer      │  │  (Stateless)        │ │  │
│  │  │  RBAC       │  │  XpService  │  │  Bitwise Processing │ │  │
│  │  └─────────────┘  └──────┬──────┘  └─────────────────────┘ │  │
│  │                          │                                  │  │
│  │  ┌───────────────────────▼──────────────────────────────┐  │  │
│  │  │  Spring Data JPA  ·  Flyway Migrations               │  │  │
│  └──┴──────────────────────────────────────────────────────┴──┘  │
│                            │                                      │
│  ┌─────────────────────────▼──────────────────┐                  │
│  │  PostgreSQL 16                              │                  │
│  └─────────────────────────────────────────────┘                  │
└───────────────────────────────────────────────────────────────────┘
```

### Decisões de Design

**Autenticação Stateless (JWT)**
Todo estado de sessão reside exclusivamente no token JWT assinado pelo cliente. O servidor não mantém sessões em memória, o que garante horizontalidade e elimina dependência de sticky sessions em cenários de escalabilidade horizontal.

**Processamento do Simulador via Snapshots**
A engine do simulador de cache processa cada passo da sequência de acessos à memória no servidor usando operadores bitwise nativos do Java (`>>>`, `&`, `<<`) e devolve ao frontend um array imutável de *snapshots* de estado. O frontend é responsável exclusivamente pela renderização — sem lógica de hardware no cliente.

**Controle de Concorrência por Lock Pessimista**
A atualização do XP do usuário usa `SELECT ... FOR UPDATE` (via `@Lock(LockModeType.PESSIMISTIC_WRITE)` do JPA), garantindo serialização da transação ao nível de banco de dados e eliminando a *race condition* que poderia causar perda silenciosa de pontos em requisições paralelas.

**Integridade de Dados por Exclusão Lógica**
Questões e alternativas nunca são removidas fisicamente do banco. A flag `ativa = false` desativa o conteúdo para novos alunos, preservando o histórico de tentativas e garantindo que métricas de desempenho retroativas permaneçam íntegras.

## Stack Tecnológica

### Backend

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 4.1 | Framework de aplicação |
| Spring Security | 6.x | Autenticação JWT e RBAC |
| Spring Data JPA | 3.x | Camada de persistência |
| PostgreSQL | 16 | Banco de dados relacional |
| Flyway | 10.x | Versionamento e migração de schema |
| SpringDoc OpenAPI | 2.x | Documentação interativa (Swagger UI) |
| JUnit 5 + Mockito | — | Cobertura de testes (115 casos) |
| Lombok | — | Redução de boilerplate |

### Frontend

| Tecnologia | Versão | Uso |
|---|---|---|
| Angular | 21 | Framework SPA |
| TypeScript | 5.x | Linguagem principal |
| PrimeNG Community | — | Biblioteca de componentes de UI |
| Chart.js | 4.x | Visualização de dados (Hit/Miss, Tendências) |
| SCSS | — | Estilização com design system proprietário |
| Angular Signals | — | Gerenciamento de estado reativo |

## Estrutura do Repositório

```
Cachly/
├── backend/                    # API REST — Java / Spring Boot
│   ├── src/main/java/
│   │   └── br/com/cachly/backend/
│   │       ├── alternativa/    # CRUD de alternativas
│   │       ├── categoria/      # CRUD de categorias de conteúdo
│   │       ├── comum/          # Exceções, DTOs globais, segurança
│   │       ├── questao/        # CRUD e lógica de questões
│   │       ├── resposta/       # Engine de respostas, XP e ofensivas
│   │       ├── simulador/      # Engine stateless de simulação de cache
│   │       └── usuario/        # Autenticação, alunos e ranking
│   ├── src/main/resources/
│   │   └── db/migration/       # Scripts Flyway (V1 → V7)
│   └── README.md
│
├── frontend/                   # SPA — Angular 21
│   ├── src/app/
│   │   ├── core/               # Guards, Interceptors, Serviços HTTP
│   │   ├── features/           # Módulos de funcionalidade (simulador, questões, perfil)
│   │   └── shared/             # Componentes e pipes reutilizáveis
│   └── README.md
│
└── README.md
```

Para instruções de configuração, variáveis de ambiente e execução local, consulte os READMEs internos de cada aplicação:

- [Configuração do Backend](backend/README.md)
- [Configuração do Frontend](frontend/README.md)

## Qualidade e Testes

A suíte de testes cobre os fluxos críticos de negócio com testes **unitários** (Mockito) e **de integração** (MockMvc com perfil `test`).

| Escopo | Casos | Status |
|---|---|---|
| `RespostaService` (XP, Ofensiva, Nível) | 6 | Passing |
| `RespostaController` (Rotas REST) | — | Passing |
| `AutenticacaoController` | 4 | Passing |
| `AutenticacaoIntegracaoTest` | 2 | Passing |
| `UsuarioServiceTest` | 6 | Passing |
| `RankingServiceTest` | 2 | Passing |
| **Total** | **115** | **0 Failures** |

Para executar a suíte completa:

```bash
cd backend
./mvnw test
```

## Licença

Distribuído sob a licença MIT. Consulte o arquivo `LICENSE` para detalhes.

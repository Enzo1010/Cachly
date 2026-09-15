# Frontend do Cachly

Aplicação web do Cachly, uma plataforma educacional gamificada para o estudo de Sistemas Digitais
e Arquitetura de Computadores. O frontend concentra as telas, a navegação e a interação do usuário
com a API REST do backend.

## Tecnologias

- Angular 21 com componentes standalone;
- TypeScript em modo estrito;
- Angular Router;
- formulários reativos;
- PrimeNG Community 21;
- PrimeIcons;
- SCSS;
- Vitest para testes unitários.

O PrimeNG é a biblioteca visual padrão. Sua adoção deve ser gradual, utilizando somente os
componentes necessários para cada funcionalidade. O tema global parte do preset Aura e aplica a
paleta visual do Cachly.

## Pré-requisitos

- Node.js compatível com Angular 21;
- npm.

## Instalação

Dentro da pasta `frontend`, instale as dependências:

```bash
npm install
```

## Executando localmente

```bash
npm start
```

A aplicação ficará disponível em `http://localhost:4200/`.

## Testes

Para executar os testes uma única vez:

```bash
npm test -- --watch=false
```

## Build de produção

```bash
npm run build
```

Os arquivos gerados serão armazenados em `dist/cachly-frontend/`.

## Organização do código

```text
src/app/
├── core/       # Configurações e recursos globais únicos
├── features/   # Funcionalidades organizadas por domínio
├── app.config.ts
└── app.routes.ts
```

A autenticação inicial está em `features/auth/`. O tema do PrimeNG está centralizado em
`core/config/tema-cachly.ts` e é registrado globalmente no `app.config.ts`.

As regras completas de desenvolvimento do frontend estão documentadas no arquivo `AGENTS.md`.

## Estado atual

O frontend está totalmente funcional e integrado ao backend. O ecossistema inclui:

- **Autenticação JWT stateless**: Login, persistência de sessão e proteção de rotas com guards.
- **Painel de Perfil**: Modal para alterar senha e exibir estatísticas consolidadas de XP e ranking.
- **Estudo e Gamificação**: Listagem de questões, submissão de respostas, acúmulo de XP, níveis e ofensiva de estudos.
- **Liga Semanal (Ranking)**: Tabela de classificação em tempo real mostrando os usuários mais dedicados da semana.
- **Simulador de Memória Cache**: Engine interativa com step-by-step, além de um sistema de Desafios práticos que pontuam experiência.
- **Administração**: CRUD de categorias e questões protegidos por RBAC.

O desenvolvimento atual utiliza o Nginx ou o `proxy.conf.json` (`npm start`) para espelhar as requisições `/api` para o backend rodando em `http://localhost:8080`.

O projeto poderá ser utilizado futuramente como base para um aplicativo Android/iOS com Capacitor, mantendo a mesma base de código Angular.

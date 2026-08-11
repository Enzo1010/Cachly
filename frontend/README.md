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

O frontend possui uma tela de login responsiva integrada ao endpoint `POST /api/auth/login`. A
sessão do usuário protege o dashboard no navegador e pode durar apenas durante a aba ou persistir
quando a opção "Lembrar login" estiver marcada. Durante o desenvolvimento, `npm start` encaminha
as requisições `/api` para o backend em `http://localhost:8080`.

O backend já expõe os endpoints para **cadastro de alunos**, **gerenciamento de sessões de estudo (quiz)** e **respostas**, que serão integrados ao frontend nas próximas etapas.

O projeto poderá ser utilizado futuramente como base para um aplicativo Android com Capacitor.

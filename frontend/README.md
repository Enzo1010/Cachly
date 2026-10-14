# Frontend do Cachly

Aplicação web do Cachly, uma plataforma educacional gamificada para o estudo de Sistemas Digitais
e Arquitetura de Computadores. O frontend concentra as telas, a navegação e a interação do usuário
com a API REST do backend.

## Tecnologias

- Angular 21 com componentes standalone e Angular Signals (`signal`, `computed`);
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

## Executando o Projeto

As instruções de instalação das dependências do Node, inicialização local do servidor de desenvolvimento Angular, execução de testes (Vitest) e build de produção encontram-se no guia consolidado do projeto:

**[Guia de Execução (RUNNING.md)](../docs/RUNNING.md)**

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

- **Autenticação JWT stateless**: Login, persistência de sessão via cookie HttpOnly e proteção de rotas com guards.
- **Recuperação de Senha**: Fluxo completo com solicitação por e-mail e redefinição com token.
- **Painel de Perfil**: Modal para alterar senha e exibir estatísticas consolidadas de XP, nível e ranking.
- **Estudo e Gamificação**: Listagem de questões, submissão de respostas, acúmulo de XP, níveis e ofensiva de estudos.
- **Fórum de Dúvidas**: Discussão em cada questão com envio de dúvidas e sistema de upvotes.
- **Liga Semanal (Ranking)**: Tabela de classificação em tempo real mostrando os usuários mais dedicados da semana e histórico arquivado.
- **Simulador de Memória Cache**: Engine interativa com visualização gráfica step-by-step e desafios práticos que pontuam XP único.
- **Administração**: CRUD completo de categorias, questões e desafios do simulador com controle RBAC.

O desenvolvimento atual utiliza o Nginx ou o `proxy.conf.json` (`npm start`) para espelhar as requisições `/api` para o backend rodando em `http://localhost:8080`.

O projeto poderá ser utilizado futuramente como base para um aplicativo Android/iOS com Capacitor, mantendo a mesma base de código Angular.

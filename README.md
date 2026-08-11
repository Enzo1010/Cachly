<div align="center">
  <img src="frontend/public/cachly-logo-sem-fundo.png" alt="Cachly Logo" width="300"/>
</div>

# Cachly

O Cachly é uma plataforma educacional gamificada para o estudo de Sistemas
Digitais e Arquitetura de Computadores. O projeto busca tornar a prática desses
conteúdos mais acessível por meio de questões de múltipla escolha, correções
explicadas e acompanhamento do progresso do estudante.

## Objetivo

Permitir que estudantes pratiquem conteúdos técnicos, recebam retorno imediato
sobre suas respostas e acompanhem sua evolução ao longo do tempo.

Os conteúdos abrangidos incluem:

- sistemas de numeração e conversão de bases;
- álgebra booleana e portas lógicas;
- tabelas verdade e circuitos digitais;
- CPU, ULA e registradores;
- memória, cache e barramentos;
- pipeline e assembly.

## Funcionalidades

- questões de múltipla escolha organizadas por categoria e dificuldade;
- correção imediata acompanhada de explicação;
- histórico de questões respondidas;
- desempenho por categoria;
- experiência, níveis e progressão;
- repetição de questões com redução da pontuação;
- ranking baseado no XP obtido pelo estudante.

## Perfis de usuário

### Aluno

Pode criar sua conta, responder questões e acompanhar desempenho, experiência
e progressão no ranking.

### Administrador

Responsável pelo gerenciamento de categorias, questões, alternativas e
explicações. Conteúdos são desativados em vez de excluídos, preservando o
histórico do sistema.

## Arquitetura

O Cachly utiliza uma arquitetura cliente-servidor:

- o backend concentra regras de negócio, persistência e API REST;
- o frontend concentra telas, navegação e interação com o usuário;
- o PostgreSQL armazena os dados da aplicação;
- frontend e backend se comunicam por JSON através da API REST;
- uma versão Android poderá ser criada futuramente com Capacitor, reutilizando
  a aplicação Angular.

## Tecnologias

### Backend

- Java 21
- Spring Boot 4.1
- Maven
- Spring Security (JWT stateless)
- Spring Data JPA
- PostgreSQL
- Flyway
- SpringDoc OpenAPI (Swagger UI)

### Frontend

- Angular 21
- TypeScript
- npm
- PrimeNG Community
- PrimeIcons
- SCSS
- Capacitor planejado para Android

## Estrutura do repositório

```text
Cachly/
├── backend/    # API REST, regras de negócio e acesso ao banco
├── frontend/   # Aplicação Angular
└── README.md   # Visão geral do projeto
```

As instruções de configuração e execução estão separadas por aplicação:

- [Documentação do backend](backend/README.md)
- [Documentação do frontend](frontend/README.md)

## Estado atual

O projeto está em desenvolvimento contínuo. No backend já estão concluídos:

- **Infraestrutura:** Conexão com PostgreSQL, versionamento do banco com Flyway e health check da aplicação.
- **CRUD completo:** Gerenciamento de categorias, questões e alternativas, com validações e respostas de erro padronizadas.
- **Segurança e autenticação:** Autenticação stateless via JWT. Controle de acesso baseado em perfil (RBAC): rotas de modificação exigem perfil `ADMINISTRADOR`; rotas de leitura e respostas são acessíveis ao `ALUNO`.
- **Core de negócio:** Fluxo de respostas, registro de tentativas, sistema de XP e endpoint otimizado para sessões de estudo (sem expor gabaritos ao cliente).
- **Desempenho e Ranking:** Endpoint de desempenho por categoria e ranking geral baseado em XP.
- **Documentação interativa:** Swagger UI disponível em `/swagger-ui.html` com suporte a autenticação JWT.
- **Qualidade:** Cobertura de testes automatizados unitários e de integração para todas as regras implementadas.

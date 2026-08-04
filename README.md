# Questly

O Questly é uma plataforma educacional gamificada para o estudo de Sistemas
Digitais e Arquitetura de Computadores. O projeto busca tornar a prática desses
conteúdos mais acessível por meio de questões de múltipla escolha, correções
explicadas e acompanhamento do progresso do estudante.

## Objetivo

O objetivo principal é permitir que estudantes pratiquem conteúdos técnicos,
recebam retorno imediato sobre suas respostas e acompanhem sua evolução ao
longo do tempo.

Os conteúdos poderão incluir:

- sistemas de numeração e conversão de bases;
- álgebra booleana e portas lógicas;
- tabelas verdade e circuitos digitais;
- CPU, ULA e registradores;
- memória, cache e barramentos;
- pipeline e assembly.

## Funcionalidades planejadas

- questões de múltipla escolha organizadas por categoria e dificuldade;
- correção imediata acompanhada de explicação;
- histórico de questões respondidas;
- desempenho por categoria;
- experiência, níveis e progressão;
- repetição de questões com redução da pontuação;
- ranking e ligas semanais baseadas no XP obtido pelo estudante.

## Perfis de usuário

### Aluno

Poderá criar sua conta, responder questões e acompanhar desempenho, experiência
e progressão.

### Administrador

Será responsável pelo gerenciamento de categorias, questões, alternativas e
explicações. Conteúdos deixam de ser exibidos por desativação, preservando o
histórico do sistema.

## Arquitetura

O Questly utiliza uma arquitetura cliente-servidor:

- o backend concentra regras de negócio, persistência e API REST;
- o frontend concentra telas, navegação e interação com o usuário;
- o PostgreSQL armazena os dados da aplicação;
- frontend e backend se comunicam por JSON através da API;
- uma versão Android poderá ser criada futuramente com Capacitor, reutilizando
  a aplicação Angular.

## Tecnologias

### Backend

- Java 21
- Spring Boot 4.1
- Maven
- Spring Data JPA
- PostgreSQL
- Flyway
- API REST

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
Questly/
├── backend/    # API REST, regras de negócio e acesso ao banco
├── frontend/   # Aplicação Angular
└── README.md   # Visão geral do projeto
```

As instruções de configuração e execução estão separadas por aplicação:

- [Documentação do backend](backend/README.md)
- [Documentação do frontend](frontend/README.md)

## Estado atual

O projeto está em desenvolvimento. No backend já estão disponíveis:

- conexão com PostgreSQL e versionamento do banco com Flyway;
- modelo inicial de usuários, categorias, questões, alternativas e tentativas;
- health check da aplicação;
- gerenciamento de categorias;
- gerenciamento de questões;
- validações e respostas de erro padronizadas;
- testes automatizados das regras e endpoints implementados.

As próximas etapas incluem o gerenciamento de alternativas, o cadastro de
alunos, a autenticação, o registro de respostas e as regras de progressão.

# AGENTS.md — Cachly Backend

## Sobre o projeto

O Cachly é uma plataforma educacional para auxiliar no estudo de Sistemas Digitais e Arquitetura de Computadores.

Este repositório contém o backend da aplicação, desenvolvido com Java, Spring Boot e PostgreSQL.

Antes de iniciar qualquer tarefa, leia:

* `README.md`, caso exista;
* os arquivos relacionados ao módulo que será alterado.

## Regras de trabalho

* Trabalhar em etapas pequenas e verificáveis.
* Implementar somente o que foi solicitado.
* Não adicionar funcionalidades extras.
* Não alterar módulos que não estejam relacionados à tarefa.
* Manter a arquitetura simples.
* Reutilizar os padrões já existentes no projeto.
* Evitar abstrações prematuras.
* Não adicionar bibliotecas sem necessidade.
* Não modificar migrations já aplicadas.
* Criar uma nova migration quando houver alteração no banco.
* Não expor entidades diretamente pela API.
* Utilizar DTOs para entrada e saída de dados.
* Manter as regras de negócio na camada de serviço.
* Manter controllers responsáveis apenas pelo fluxo HTTP.
* Tratar erros seguindo o padrão global já existente.
* Escrever nomes de classes, métodos e variáveis em portugues.
* Manter mensagens e documentação em português quando esse for o padrão existente.

## Planejamento e Kanban

* Antes de iniciar uma nova atividade, discutir e negociar o card com o usuário.
* O card deve ter título, descrição, critérios de aceite, prioridade, dependências e itens fora do escopo.
* Não iniciar a implementação até que o escopo do card esteja acordado e o usuário autorize o início.
* Registrar formalmente no Forge as dependências entre cards quando uma atividade depender de outra.
* Não iniciar um card bloqueado enquanto sua dependência não estiver concluída.
* Manter apenas um card do backend em **In Progress** por vez.
* Ao começar uma atividade autorizada, confirmar que o card correspondente foi movido de **To Do** para **In Progress**.
* Um card somente pode ser considerado **Done** quando os critérios de aceite forem atendidos, os testes passarem e o código autorizado estiver commitado e enviado ao Forge.
* Se ainda faltar autorização para commit ou push, informar que o card está tecnicamente pronto, mas não deve ser fechado.
* Após concluir um card, parar e negociar o próximo antes de alterar novos arquivos.

## Fluxo antes de alterar código

1. Ler o código relacionado à tarefa.
2. Identificar os padrões existentes.
3. Apresentar um plano curto.
4. Implementar somente a etapa solicitada.
5. Executar os testes.
6. Revisar o diff.
7. Informar os arquivos alterados e os resultados dos testes.

## Testes

* Toda nova regra de negócio deve possuir teste automatizado quando aplicável.
* Dar preferência a testes simples e focados.
* Não criar testes que dependam de ordem de execução.
* Não remover ou enfraquecer testes existentes para fazer a implementação passar.
* Antes de concluir, executar:

```bash
./mvnw test
```

Caso o projeto não possua Maven Wrapper, utilizar:

```bash
mvn test
```

## Git e Forge

* Não fazer push para o Forge sem autorização explícita.
* Não criar merge request sem autorização explícita.
* Não realizar merge.
* Não alterar o histórico do Git.
* Não usar `git push --force`.
* Antes de criar um commit, apresentar o resumo das alterações.
* Antes de criar um commit, perguntar e confirmar com o usuário o número do card relacionado.
* Nunca presumir ou inventar o número do card.
* Utilizar `Refs #NUMERO` no corpo de commits intermediários que estejam relacionados ao card, mas ainda não concluam todos os critérios de aceite.
* Utilizar `Closes #NUMERO` somente no commit final, quando todos os critérios de aceite forem atendidos e os testes estiverem passando.
* Após enviar o commit final para a branch principal, confirmar que o Forge relacionou o commit e fechou o card automaticamente.
* Depois do push, orientar a movimentação do card para **Done** somente se o fechamento tiver sido confirmado.
* Ao concluir um card, apresentar um comentário final contendo o hash do commit, um resumo do que foi implementado e o resultado dos testes.
* Somente criar commit quando houver autorização explícita.
* Utilizar Conventional Commits quando autorizado.

Exemplos:

```text
feat: adiciona gerenciamento de categorias
test: adiciona testes do módulo de categorias
fix: corrige validação de categoria
refactor: simplifica serviço de categorias
```

Exemplo de commit intermediário:

```text
test: adiciona cenários iniciais de alternativas

Refs #17
```

Exemplo de commit final:

```text
feat: implementa gerenciamento de alternativas

Closes #17
```

Exemplo de comentário final no card:

```text
Atividade concluída no commit HASH.

- resumo do que foi implementado;
- testes automatizados adicionados;
- QUANTIDADE testes executados com sucesso.
```

## Limites de autonomia

O Codex pode:

* Ler arquivos;
* Alterar arquivos relacionados à tarefa;
* Criar testes;
* Executar comandos locais de build e testes;
* Apresentar sugestões.

O Codex não pode, sem autorização:

* Fazer push;
* Criar ou enviar commits;
* Alterar configurações de produção;
* Apagar migrations;
* Trocar tecnologias do projeto;
* Adicionar novas funcionalidades;
* Fazer grandes refatorações;
* Alterar contratos da API fora do escopo solicitado.

## Finalização de cada tarefa

Ao concluir, informar:

1. O que foi implementado.
2. Quais arquivos foram criados ou alterados.
3. Quais testes foram executados.
4. Se todos os testes passaram.
5. Quais decisões técnicas foram tomadas.
6. Se existe alguma pendência.

Depois disso, parar e aguardar novas instruções.

# AGENTS.md — Frontend do Questly

Este arquivo define o padrão de trabalho para alterações dentro de `frontend/`.
As regras valem para pessoas e agentes que contribuírem com a aplicação Angular.

## Contexto do frontend

O frontend é a interface web do Questly e consumirá a API REST do backend. Ele também será a
base de um futuro aplicativo Android com Capacitor, portanto as telas devem ser responsivas e não
podem depender de comportamentos exclusivos de desktop.

Antes de iniciar uma alteração, leia:

- o `README.md` da raiz do repositório;
- o `frontend/README.md`;
- este `frontend/AGENTS.md`;
- os arquivos da funcionalidade que será modificada.

## Tecnologias utilizadas atualmente

- Angular 21.2 com componentes standalone;
- TypeScript 5.9 em modo estrito;
- Angular Router;
- Angular Signals;
- RxJS 7.8;
- Angular Forms disponível no projeto;
- PrimeNG Community compatível com Angular 21 como biblioteca visual padrão;
- PrimeIcons como biblioteca de ícones opcional;
- SCSS;
- Vitest com jsdom para testes unitários;
- npm 11.6 como gerenciador de pacotes;
- Prettier e EditorConfig para formatação.

O PrimeNG Community é a biblioteca de componentes aprovada para o projeto. Não adicione outra
biblioteca de componentes, framework CSS, gerenciador global de estado ou framework de testes
end-to-end sem que a necessidade tenha sido discutida e incluída no card.



## Planejamento dos cards

Antes de iniciar uma nova atividade, alinhe:

- objetivo e critérios de aceite;
- telas, estados e fluxos esperados;
- endpoints e contratos da API envolvidos;
- dependências em relação ao backend ou a outros cards do frontend;
- o que fica explicitamente fora do escopo;
- número do card e pessoa responsável.

Cada responsável deve manter apenas um card próprio em `In Progress`. Cards dependentes devem
registrar a dependência no Forge e permanecer fora de `In Progress` até estarem desbloqueados.

Uma tarefa administrativa pode ser feita sem card somente quando o usuário autorizar isso de forma
explícita.

## Organização do código

Adote organização por funcionalidade dentro de `src/app/features/`.

Cada funcionalidade deve conter apenas as partes de que realmente precisa, por exemplo:

```text
features/
  categorias/
    components/
    pages/
    services/
    models/
    categorias.routes.ts
```

- Não crie pastas vazias apenas para antecipar uma arquitetura futura.
- Use `core/` somente para recursos globais e únicos, como interceptadores, guards e serviços de
  sessão.
- Use `shared/` somente para componentes, diretivas ou utilitários reutilizados por mais de uma
  funcionalidade.
- Mantenha o componente raiz simples, responsável pelo shell da aplicação e pelo `router-outlet`.
- Prefira carregamento lazy para rotas de funcionalidades quando elas forem criadas.
- Não introduza NgModules; mantenha o padrão standalone já adotado pelo projeto.

## Nomes e estilo de código

- Use nomes de domínio em português, sem acentos, em classes, métodos, variáveis e rotas.
- Use `kebab-case` nos nomes de arquivos e mantenha sufixos claros, como `.component.ts`,
  `.service.ts`, `.routes.ts` e `.spec.ts`.
- Preserve os nomes exigidos pelo Angular e por APIs externas quando traduzi-los causar conflito
  técnico.
- Não use `any`; modele os dados com tipos ou interfaces explícitos.
- Respeite o modo estrito do TypeScript e não desative verificações para contornar erros.
- Use `readonly` quando um valor não precisar ser reatribuído.
- Siga a formatação existente: dois espaços, aspas simples em TypeScript e largura de 100
  caracteres.
- Evite arquivos e componentes grandes; extraia responsabilidades somente quando houver uma
  separação real de comportamento ou reutilização.

## Estado, formulários e assincronismo

- Use Signals para estado local e síncrono da interface.
- Use RxJS para fluxos assíncronos, especialmente chamadas HTTP.
- Não mantenha o mesmo estado simultaneamente em Signal e Observable sem uma justificativa clara.
- Prefira formulários reativos em telas com validação ou regras de negócio.
- Mostre estados de carregamento, vazio, sucesso e erro quando a tela depender da API.

## Componentes e estilos

- Use o PrimeNG Community compatível com Angular 21 como padrão para componentes de interface.
- Adote os componentes do PrimeNG gradualmente, apenas quando forem necessários para a tela ou
  funcionalidade em desenvolvimento; não reescreva componentes existentes sem benefício claro.
- Personalize o tema do PrimeNG com a identidade visual e os tokens do Questly, evitando vincular
  a aplicação à aparência do Angular Material.
- Use apenas os pacotes comunitários gratuitos. Pacotes LTS, templates, blocos ou outros recursos
  pagos não devem ser adicionados sem aprovação explícita.
- O PrimeIcons pode ser utilizado quando houver necessidade de ícones; não adicione outra
  biblioteca de ícones sem aprovação no card.
- Componentes devem ter uma responsabilidade clara e uma API pequena.
- Mantenha template e SCSS junto do componente; estilos globais devem conter apenas base visual,
  tokens e regras realmente compartilhadas.
- Evite estilos inline extensos e valores visuais repetidos espalhados pelo projeto.
- Garanta navegação por teclado, foco visível, HTML semântico, rótulos de formulário e contraste
  adequado.
- Valide as telas em larguras de celular e desktop.
- Não adicione outra biblioteca visual ou de ícones sem aprovação no card.

## Integração com o backend

- Centralize chamadas HTTP em serviços da funcionalidade.
- Não faça chamadas HTTP diretamente em componentes de apresentação.
- Não fixe a URL da API em vários arquivos; use uma configuração central quando a integração for
  implementada.
- Mantenha os modelos de requisição e resposta compatíveis com os DTOs do backend.
- Não armazene senhas, segredos ou tokens no código-fonte.
- Não invente endpoints nem campos que não existam no contrato acordado com o backend.

## Testes e verificação

- Use Vitest e mantenha os testes próximos do arquivo testado com o sufixo `.spec.ts`.
- Teste comportamentos observáveis, validações, navegação e tratamento de estados; evite testar
  detalhes internos sem valor para o usuário.
- Toda correção de defeito deve incluir um teste que reproduza o problema quando isso for viável.
- Antes de concluir uma alteração de código, execute:

```bash
npm test -- --watch=false
npm run build
```

Uma alteração apenas de documentação não exige a execução desses comandos, mas isso deve ser
informado na entrega.

## Git e Forge

- Verifique `git status` antes de modificar ou preparar arquivos.
- Preserve alterações existentes que não façam parte do card.
- Não misture frontend, backend e documentação no mesmo commit sem necessidade explícita.
- Antes de criar um commit, peça ou confirme o número do card.
- Escreva a mensagem do commit em português e descreva uma única mudança coerente.
- Use `Refs #NUMERO` quando o commit estiver relacionado ao card, mas ainda não o concluir.
- Use `Closes #NUMERO` somente quando todos os critérios de aceite estiverem atendidos e o commit
  realmente concluir o card.
- Não faça push sem autorização explícita.
- Não use push forçado sem autorização explícita e uma justificativa segura.

## Critério de conclusão

Uma atividade só está pronta quando:

- todos os critérios de aceite foram conferidos;
- testes e build aplicáveis passaram;
- a interface foi verificada visualmente nos estados relevantes;
- não foram adicionadas funcionalidades fora do card;
- os arquivos alterados e eventuais limitações foram informados;
- o número correto do card foi usado no commit, quando houver commit.

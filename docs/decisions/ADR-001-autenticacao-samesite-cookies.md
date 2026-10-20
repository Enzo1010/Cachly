# ADR-001: Autenticação via Cookies SameSite=Strict e Desativação do CSRF

## Status
Aceito (Accepted)

## Contexto
O Cachly é composto por uma arquitetura desacoplada com backend em Spring Boot 4.1 e frontend Single Page Application (SPA) em Angular 21. A autenticação do usuário é efetuada por meio de JSON Web Tokens (JWT).

Em aplicações web baseadas em cookies de sessão clássicas, mecanismos de proteção contra Cross-Site Request Forgery (CSRF) como o *Synchronizer Token Pattern* ou *Double Submit Cookie* são tradicionalmente empregados pelo Spring Security. Contudo, implementar e sincronizar tokens CSRF em aplicações stateless com SPA adiciona overhead de endpoints, headers adicionais e complexidade de manutenção.

## Decisão
1. Desativar explicitamente a proteção CSRF do Spring Security (`.csrf(csrf -> csrf.disable())`) no `SecurityConfig.java`.
2. Proteger as requisições autenticadas por meio do envio do JWT em cookie com as seguintes diretivas obrigatórias configuradas em `AutenticacaoController.java`:
   - **`SameSite=Strict`**: Impede que navegadores enviem o cookie em contextos cross-site. É a mitigação de CSRF adotada para o modelo atual de frontend e API no mesmo site; não substitui uma estratégia CSRF se essa topologia mudar.
   - **`HttpOnly=true`**: Impede leitura do cookie por JavaScript e reduz o risco de exfiltração do token. Não impede que um XSS execute requisições autenticadas no mesmo site.
   - **`Secure=true`**: Assegura a transmissão exclusiva sob conexões HTTPS criptografadas.
3. Permitir de forma complementar a autenticação via cabeçalho `Authorization: Bearer <token>` para clientes de API e ferramentas de teste.

## Consequências
- **Positivas**: Elimina o gerenciamento de tokens CSRF no cliente Angular e simplifica a esteira de requisições no cenário same-site atual.
- **Limitações/Cuidados**: A proteção depende de `SameSite=Strict`, HTTPS e da manutenção de frontend e API no mesmo site. Caso a topologia passe a exigir cookies cross-site, a decisão deve ser revisada e uma defesa CSRF explícita deve ser adotada.

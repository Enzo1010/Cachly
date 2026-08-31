# ADR-001: Autenticação via Cookies SameSite=Strict e Desativação do CSRF

## Status
Aceito (Accepted)

## Contexto
O Cachly é composto por uma arquitetura desacoplada com backend em Spring Boot 3 e frontend Single Page Application (SPA) em Angular 19. A autenticação do usuário é efetuada por meio de JSON Web Tokens (JWT).

Em aplicações web baseadas em cookies de sessão clássicas, mecanismos de proteção contra Cross-Site Request Forgery (CSRF) como o *Synchronizer Token Pattern* ou *Double Submit Cookie* são tradicionalmente empregados pelo Spring Security. Contudo, implementar e sincronizar tokens CSRF em aplicações stateless com SPA adiciona overhead de endpoints, headers adicionais e complexidade de manutenção.

## Decisão
1. Desativar explicitamente a proteção CSRF do Spring Security (`.csrf(csrf -> csrf.disable())`) no `SecurityConfig.java`.
2. Proteger as requisições autenticadas por meio do envio do JWT em cookie com as seguintes diretivas obrigatórias configuradas em `AutenticacaoController.java`:
   - **`SameSite=Strict`**: Garante que o navegador nunca anexe o cookie em requisições disparadas a partir de origens de terceiros (cross-site), eliminando o vetor de ataque CSRF.
   - **`HttpOnly=true`**: Impede que o cookie seja lido ou manipulado via scripts JavaScript (DOM/XSS).
   - **`Secure=true`**: Assegura a transmissão exclusiva sob conexões HTTPS criptografadas.
3. Permitir de forma complementar a autenticação via cabeçalho `Authorization: Bearer <token>` para clientes de API e ferramentas de teste.

## Consequências
- **Positivas**: Elimina a necessidade de gerenciamento de tokens CSRF no cliente Angular; reduz a superfície de ataque mantendo a aplicação segura no nível dos navegadores modernos; simplifica a esteira de requisições HTTP.
- **Limitações/Cuidados**: Requer suporte a cookies com atributo `SameSite` nos navegadores dos clientes (comportamento padrão em todos os navegadores modernos).
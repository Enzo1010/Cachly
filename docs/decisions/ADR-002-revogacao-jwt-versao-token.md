# ADR-002: Revogação de JWT por Coluna `versao_token`

## Status
Aceito (Accepted)

## Contexto
Por padrão, tokens JWT são auto-contidos e *stateless*, permanecendo criptograficamente válidos até atingirem sua data de expiração (`exp`). Contudo, fluxos críticos de segurança exigem a invalidação antecipada de tokens ativos, tais como:
- Alteração ou redefinição de senha do usuário (ex.: suspeita de vazamento).
- Logout explícito em todas as sessões / encerramento remoto de dispositivos.
- Desativação ou bloqueio administrativo da conta.

Manter uma lista negra centralizada em memória (ex.: Redis Blacklist) adiciona complexidade de infraestrutura e sobrecarga de gerenciamento de chaves para um sistema de médio porte.

## Decisão
Implementar um mecanismo de revogação quase-stateless com base em versão temporal:
1. Adicionar o campo `versaoToken` (timestamp em milissegundos) na entidade `Usuario`.
2. Incluir a claim padrão de emissão `iat` (*issued at*) no payload do JWT gerado pelo `TokenService`.
3. Ao executar operações sensíveis (como `POST /api/auth/alterar-senha` ou `POST /api/auth/revogar-sessoes`), atualizar `versaoToken` para o timestamp corrente (`System.currentTimeMillis()`) sob consistência transacional.
4. No `SecurityFilter` e `UsuarioLogadoService`, validar a sessão do usuário executando uma consulta rápida por projeção (`usuarioRepository.findVersaoTokenById(id)`):
   - Se `iat < (versaoToken - 1000)`, o token é considerado revogado e a requisição é rejeitada com status **HTTP 401 Unauthorized**. A margem de 1.000 ms acomoda a precisão temporal da claim JWT e cria uma tolerância máxima aproximada de um segundo.

## Consequências
- **Positivas**: Invalida tokens emitidos antes do evento de segurança sem necessidade de storage distribuído de blacklist; a consulta usa a chave primária `id`.
- **Limitações/Cuidados**: Requer uma consulta leve ao banco por requisição autenticada e não é instantânea em sentido estrito devido à tolerância de até um segundo.

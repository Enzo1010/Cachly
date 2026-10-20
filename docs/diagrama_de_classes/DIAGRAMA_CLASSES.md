# Diagrama de Classes (Modelo de Domínio)

O diagrama abaixo ilustra o modelo de domínio principal (Entidades JPA) do backend do Cachly, demonstrando como os dados estão relacionados e persistidos no banco de dados.

```mermaid
classDiagram
    direction LR

    class EntidadeAuditavel {
        <<MappedSuperclass>>
        +OffsetDateTime criadoEm
        +OffsetDateTime atualizadoEm
        +Long criadoPor
        +Long atualizadoPor
    }

    class Categoria {
        +Long id
        +String nome
        +String descricao
        +Boolean ativa
    }
    
    class Questao {
        +Long id
        +String enunciado
        +String explicacao
        +Integer xpBase
        +Boolean ativa
    }

    class DificuldadeQuestao {
        <<Enumeration>>
        FACIL
        MEDIO
        DIFICIL
    }

    class Alternativa {
        +Long id
        +String texto
        +Boolean correta
        +Short ordem
        +Boolean ativa
    }

    class Usuario {
        +Long id
        +String nome
        +String email
        +String senhaHash
        +Integer xpTotal
        +Integer xpSemanal
        +Integer nivel
        +Boolean ativo
        +Integer diasOfensiva
        +LocalDate dataUltimaOfensiva
        +Long versaoToken
        +String tokenReset
        +OffsetDateTime tokenResetExpiraEm
        +OffsetDateTime criadoEm
        +OffsetDateTime atualizadoEm
    }

    class PerfilUsuario {
        <<Enumeration>>
        ALUNO
        ADMINISTRADOR
    }

    class TentativaQuestao {
        +Long id
        +Boolean correta
        +Integer xpConcedido
        +OffsetDateTime respondidaEm
    }

    class DesafioConcluido {
        +Long id
        +String desafioId
        +OffsetDateTime dataConclusao
    }

    class RankingSemanalHistorico {
        +Long id
        +Integer xpFinal
        +Integer posicao
        +LocalDate dataSemana
    }

    class Comentario {
        +Long id
        +String conteudo
        +Integer upvotes
    }

    class DesafioSimulador {
        +String id
        +String titulo
        +String descricao
        +Integer xpRecompensa
        +String opcaoCorretaId
        +String dica
        +String explicacao
    }

    class OpcaoDesafioEmbeddable {
        <<Embeddable>>
        +String id
        +String texto
    }

    %% Herança (Auditoria)
    EntidadeAuditavel <|-- Categoria
    EntidadeAuditavel <|-- Questao
    EntidadeAuditavel <|-- Alternativa
    EntidadeAuditavel <|-- Comentario
    EntidadeAuditavel <|-- DesafioSimulador

    %% Composição do Conteúdo
    Categoria "1" *-- "*" Questao : possui
    Questao "1" *-- "*" Alternativa : contém
    Questao --> DificuldadeQuestao : nível

    %% Interação do Usuário com Conteúdo
    Usuario "1" --> "*" TentativaQuestao : realiza
    TentativaQuestao "*" --> "1" Questao : avalia
    TentativaQuestao "*" --> "1" Alternativa : selecionada
    Questao "1" --> "*" Comentario : recebe
    Usuario "1" --> "*" Comentario : publica

    %% Gamificação e Conta
    Usuario --> PerfilUsuario : perfil
    Usuario "1" --> "*" DesafioConcluido : conclui
    Usuario "1" --> "*" RankingSemanalHistorico : histórico
    DesafioSimulador "1" *-- "*" OpcaoDesafioEmbeddable : opções
```

## Notas da Arquitetura de Dados

- **EntidadeAuditavel**: Fornece de forma transparente rastreabilidade (quem criou/atualizou e quando) para Categoria, Questão, Alternativa, Comentário e Desafio do Simulador.
- **DesafioConcluido e TentativaQuestao**: Representam o histórico de aprendizagem e engajamento do aluno. A proteção transacional pessimista (Lock no `Usuario`) atua antes da inserção nessas tabelas para evitar inflacionamento de XP.
- **Exclusão Lógica**: Categorias, questões e alternativas usam a propriedade `ativa` (Boolean) no fluxo administrativo. Desafios do simulador são removidos fisicamente pelo endpoint administrativo.

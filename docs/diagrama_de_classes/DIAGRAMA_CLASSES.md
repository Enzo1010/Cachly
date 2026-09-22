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
        MEDIA
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

    %% Herança (Auditoria)
    EntidadeAuditavel <|-- Categoria
    EntidadeAuditavel <|-- Questao
    EntidadeAuditavel <|-- Alternativa

    %% Composição do Conteúdo
    Categoria "1" *-- "*" Questao : possui
    Questao "1" *-- "*" Alternativa : contém
    Questao --> DificuldadeQuestao : nível

    %% Interação do Usuário com Conteúdo
    Usuario "1" --> "*" TentativaQuestao : realiza
    TentativaQuestao "*" --> "1" Questao : avalia
    TentativaQuestao "*" --> "1" Alternativa : selecionada

    %% Gamificação e Conta
    Usuario --> PerfilUsuario : perfil
    Usuario "1" --> "*" DesafioConcluido : conclui
    Usuario "1" --> "*" RankingSemanalHistorico : histórico
```

## Notas da Arquitetura de Dados

- **EntidadeAuditavel**: Fornece de forma transparente rastreabilidade (quem criou/atualizou e quando) para entidades de gerenciamento de conteúdo (Categoria, Questão e Alternativa).
- **DesafioConcluido e TentativaQuestao**: Representam o histórico de aprendizagem e engajamento do aluno. A proteção transacional pessimista (Lock no `Usuario`) atua antes da inserção nessas tabelas para evitar inflacionamento de XP.
- **Exclusão Lógica**: Entidades core utilizam a propriedade `ativa` (Boolean) em vez de deleção física, preservando as integridades referenciais históricas.

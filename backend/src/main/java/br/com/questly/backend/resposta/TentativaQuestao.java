package br.com.questly.backend.resposta;

import br.com.questly.backend.alternativa.Alternativa;
import br.com.questly.backend.questao.Questao;
import br.com.questly.backend.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tentativas_questao")
@Getter
@Setter
@NoArgsConstructor
public class TentativaQuestao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alternativa_id", nullable = false)
    private Alternativa alternativa;

    @Column(nullable = false)
    private Boolean correta;

    @Column(name = "xp_concedido", nullable = false)
    private Integer xpConcedido = 0;

    @Column(name = "respondida_em", nullable = false)
    private OffsetDateTime respondidaEm;

    @PrePersist
    private void antesDeSalvar() {
        if (respondidaEm == null) {
            respondidaEm = OffsetDateTime.now();
        }
    }
}

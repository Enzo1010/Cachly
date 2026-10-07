package br.com.cachly.backend.simulador.desafio;

import br.com.cachly.backend.comum.auditoria.EntidadeAuditavel;
import br.com.cachly.backend.questao.DificuldadeQuestao;
import br.com.cachly.backend.simulador.SimulacaoRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "desafios_simulador")
@Getter
@Setter
@NoArgsConstructor
public class DesafioSimulador extends EntidadeAuditavel {

    @Id
    @Column(name = "id")
    private String id; // Using slug as ID for compatibility with completed challenges

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DificuldadeQuestao dificuldade;

    @Column(name = "xp_recompensa", nullable = false)
    private Integer xpRecompensa;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuracao_json", nullable = false)
    private SimulacaoRequest configuracao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String pergunta;

    @Column(name = "opcao_correta_id", nullable = false)
    private String opcaoCorretaId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String dica;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explicacao;

    @ElementCollection
    @CollectionTable(name = "desafios_simulador_opcoes", joinColumns = @JoinColumn(name = "desafio_id"))
    private List<OpcaoDesafioEmbeddable> opcoes = new ArrayList<>();
}

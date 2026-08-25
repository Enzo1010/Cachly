package br.com.cachly.backend.usuario.aluno;

import br.com.cachly.backend.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "ranking_semanal_historico")
@Getter
@Setter
@NoArgsConstructor
public class RankingSemanalHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "xp_final", nullable = false)
    private Integer xpFinal;

    @Column(name = "posicao", nullable = false)
    private Integer posicao;

    @Column(name = "data_semana", nullable = false)
    private LocalDate dataSemana;

    public RankingSemanalHistorico(Usuario usuario, Integer xpFinal, Integer posicao, LocalDate dataSemana) {
        this.usuario = usuario;
        this.xpFinal = xpFinal;
        this.posicao = posicao;
        this.dataSemana = dataSemana;
    }
}

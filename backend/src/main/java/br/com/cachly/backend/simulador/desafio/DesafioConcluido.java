package br.com.cachly.backend.simulador.desafio;

import br.com.cachly.backend.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "desafios_concluidos", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id", "desafio_id"})
})
@Getter
@Setter
public class DesafioConcluido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "desafio_id", nullable = false)
    private String desafioId;

    @Column(name = "data_conclusao", nullable = false)
    private OffsetDateTime dataConclusao = OffsetDateTime.now();

}

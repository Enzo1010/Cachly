package br.com.cachly.backend.questao.comentario;

import br.com.cachly.backend.usuario.PerfilUsuario;
import java.time.OffsetDateTime;

public record ComentarioResponse(
    Long id,
    String conteudo,
    Integer upvotes,
    OffsetDateTime criadoEm,
    String autorNome,
    PerfilUsuario autorPerfil,
    boolean usuarioDeuUpvote
) {
    public static ComentarioResponse fromEntity(Comentario c, Long usuarioLogadoId) {
        return new ComentarioResponse(
            c.getId(),
            c.getConteudo(),
            c.getUpvotes(),
            c.getCriadoEm(),
            c.getUsuario().getNome(),
            c.getUsuario().getPerfil(),
            c.getUpvotedBy() != null && c.getUpvotedBy().contains(usuarioLogadoId)
        );
    }
}

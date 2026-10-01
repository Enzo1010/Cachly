package br.com.cachly.backend.questao.comentario;

import br.com.cachly.backend.questao.Questao;
import br.com.cachly.backend.questao.QuestaoRepository;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioLogadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final QuestaoRepository questaoRepository;
    private final UsuarioLogadoService usuarioLogadoService;

    public List<ComentarioResponse> listarPorQuestao(Long questaoId) {
        Long usuarioLogadoId = usuarioLogadoService.obterUsuarioAtual().getId();
        return comentarioRepository.findByQuestaoIdOrderByUpvotesDescCriadoEmDesc(questaoId)
                .stream()
                .map(c -> ComentarioResponse.fromEntity(c, usuarioLogadoId))
                .toList();
    }

    @Transactional
    public ComentarioResponse adicionarComentario(Long questaoId, NovoComentarioRequest request) {
        Usuario usuarioAtual = usuarioLogadoService.obterUsuarioAtual();
        Questao questao = questaoRepository.findById(questaoId)
                .orElseThrow(() -> new IllegalArgumentException("Questão não encontrada"));

        Comentario comentario = new Comentario();
        comentario.setQuestao(questao);
        comentario.setUsuario(usuarioAtual);
        comentario.setConteudo(request.conteudo());
        comentario.setUpvotes(0);

        Comentario salvo = comentarioRepository.save(comentario);
        return ComentarioResponse.fromEntity(salvo, usuarioAtual.getId());
    }

    @Transactional
    public void darUpvote(Long comentarioId) {
        Long usuarioLogadoId = usuarioLogadoService.obterUsuarioAtual().getId();
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new IllegalArgumentException("Comentário não encontrado"));
        
        if (comentario.getUpvotedBy().contains(usuarioLogadoId)) {
            // Remove upvote se já curtiu
            comentario.getUpvotedBy().remove(usuarioLogadoId);
            comentario.setUpvotes(comentario.getUpvotes() - 1);
        } else {
            // Adiciona upvote
            comentario.getUpvotedBy().add(usuarioLogadoId);
            comentario.setUpvotes(comentario.getUpvotes() + 1);
        }
        comentarioRepository.save(comentario);
    }
}

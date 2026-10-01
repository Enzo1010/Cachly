package br.com.cachly.backend.questao.comentario;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByQuestaoIdOrderByUpvotesDescCriadoEmDesc(Long questaoId);
}

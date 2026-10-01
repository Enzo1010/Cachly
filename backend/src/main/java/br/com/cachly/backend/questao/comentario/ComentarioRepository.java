package br.com.cachly.backend.questao.comentario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    
    @Query("""
        SELECT DISTINCT c FROM Comentario c
        JOIN FETCH c.usuario
        LEFT JOIN FETCH c.upvotedBy
        WHERE c.questao.id = :questaoId
        ORDER BY c.upvotes DESC, c.criadoEm DESC
    """)
    List<Comentario> findByQuestaoIdOrderByUpvotesDescCriadoEmDesc(@Param("questaoId") Long questaoId);
}

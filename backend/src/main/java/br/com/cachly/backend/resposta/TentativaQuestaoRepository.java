package br.com.cachly.backend.resposta;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TentativaQuestaoRepository extends JpaRepository<TentativaQuestao, Long> {

    Page<TentativaQuestao> findByUsuarioIdOrderByRespondidaEmDesc(Long usuarioId, Pageable pageable);

    long countByUsuarioId(Long usuarioId);

    long countByUsuarioIdAndCorretaTrue(Long usuarioId);
    
    boolean existsByUsuarioIdAndQuestaoIdAndCorretaTrue(Long usuarioId, Long questaoId);

    long countByUsuarioIdAndRespondidaEmBetween(Long usuarioId, java.time.OffsetDateTime start, java.time.OffsetDateTime end);

    @Query("""
            SELECT c.id as categoriaId, c.nome as categoriaNome,
                   COUNT(t.id) as totalTentativas,
                   SUM(CASE WHEN t.correta = true THEN 1 ELSE 0 END) as acertos
            FROM TentativaQuestao t
            JOIN t.questao q
            JOIN q.categoria c
            WHERE t.usuario.id = :usuarioId
            GROUP BY c.id, c.nome
            ORDER BY c.nome ASC
            """)
    List<DesempenhoCategoriaProjection> findEstatisticasPorCategoria(@Param("usuarioId") Long usuarioId);
}


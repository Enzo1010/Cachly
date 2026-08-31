package br.com.cachly.backend.questao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuestaoRepository extends JpaRepository<Questao, Long> {

    /**
     * Busca todas as questões ativas junto com suas categorias em um único SELECT
     * (JOIN FETCH), evitando o problema de N+1 queries ao acessar {@code questao.getCategoria()}.
     */
    @EntityGraph(attributePaths = {"categoria", "alternativas"})
    List<Questao> findAll();

    @EntityGraph(attributePaths = {"categoria", "alternativas"})
    @Query("SELECT q FROM Questao q WHERE q.ativa = true ORDER BY q.id ASC")
    List<Questao> findAllByAtivaTrueOrderByIdAsc();

    @EntityGraph(attributePaths = {"categoria", "alternativas"})
    Optional<Questao> findByIdAndAtivaTrue(Long id);

    @EntityGraph(attributePaths = {"categoria", "alternativas"})
    List<Questao> findAllByCategoriaIdAndAtivaTrueOrderByIdAsc(Long categoriaId, Pageable pageable);

    @EntityGraph(attributePaths = {"categoria", "alternativas"})
    List<Questao> findAllByAtivaTrueOrderByIdAsc(Pageable pageable);
}

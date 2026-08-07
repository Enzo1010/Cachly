package br.com.questly.backend.questao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestaoRepository extends JpaRepository<Questao, Long> {

    List<Questao> findAllByAtivaTrueOrderByIdAsc();

    Optional<Questao> findByIdAndAtivaTrue(Long id);

    List<Questao> findAllByCategoriaIdAndAtivaTrueOrderByIdAsc(Long categoriaId, Pageable pageable);

    List<Questao> findAllByAtivaTrueOrderByIdAsc(Pageable pageable);
}

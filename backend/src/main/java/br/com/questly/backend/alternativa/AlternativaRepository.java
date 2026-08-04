package br.com.questly.backend.alternativa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlternativaRepository extends JpaRepository<Alternativa, Long> {

    List<Alternativa> findAllByQuestaoIdAndAtivaTrueOrderByOrdemAsc(Long questaoId);

    Optional<Alternativa> findByIdAndQuestaoId(Long id, Long questaoId);

    boolean existsByQuestaoIdAndOrdem(Long questaoId, Short ordem);

    boolean existsByQuestaoIdAndOrdemAndIdNot(Long questaoId, Short ordem, Long id);

    boolean existsByQuestaoIdAndCorretaTrueAndAtivaTrue(Long questaoId);

    boolean existsByQuestaoIdAndCorretaTrueAndAtivaTrueAndIdNot(
            Long questaoId,
            Long id
    );
}

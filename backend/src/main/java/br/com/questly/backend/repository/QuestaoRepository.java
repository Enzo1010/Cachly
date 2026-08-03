package br.com.questly.backend.repository;

import br.com.questly.backend.entity.Questao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestaoRepository extends JpaRepository<Questao, Long> {

    List<Questao> findAllByAtivaTrueOrderByIdAsc();
}

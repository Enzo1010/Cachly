package br.com.cachly.backend.usuario.aluno;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RankingSemanalHistoricoRepository extends JpaRepository<RankingSemanalHistorico, Long> {
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"usuario"})
    org.springframework.data.domain.Page<RankingSemanalHistorico> findByDataSemana(java.time.LocalDate dataSemana, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT r.dataSemana FROM RankingSemanalHistorico r ORDER BY r.dataSemana DESC")
    java.util.List<java.time.LocalDate> findDatasDisponiveis();
}

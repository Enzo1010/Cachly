package br.com.cachly.backend.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.time.OffsetDateTime;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Usuario u WHERE u.id = :id")
    Optional<Usuario> findByIdForUpdate(@Param("id") Long id);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Usuario> findByEmailIgnoreCase(String email);
    
    Page<Usuario> findByPerfil(PerfilUsuario perfil, Pageable pageable);

    @Query("""
        SELECT u.nome AS nome, u.nivel AS nivel, u.diasOfensiva AS diasOfensiva,
               COALESCE(SUM(t.xpConcedido), 0) AS xpSemanal
        FROM Usuario u
        LEFT JOIN TentativaQuestao t ON t.usuario.id = u.id AND t.respondidaEm >= :inicioSemana
        WHERE u.perfil = :perfil
        GROUP BY u.id, u.nome, u.nivel, u.diasOfensiva
    """)
    Page<br.com.cachly.backend.usuario.aluno.RankingProjection> findRankingSemanal(
        @Param("perfil") PerfilUsuario perfil, 
        @Param("inicioSemana") OffsetDateTime inicioSemana, 
        Pageable pageable
    );
}

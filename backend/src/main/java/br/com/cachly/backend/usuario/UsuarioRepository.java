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
        SELECT u.id AS id, u.nome AS nome, u.nivel AS nivel, u.diasOfensiva AS diasOfensiva,
               u.xpSemanal AS xpSemanal
        FROM Usuario u
        WHERE u.perfil = :perfil
        ORDER BY u.xpSemanal DESC
    """)
    Page<br.com.cachly.backend.usuario.aluno.RankingProjection> findRankingSemanal(
        @Param("perfil") PerfilUsuario perfil, 
        Pageable pageable
    );

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Usuario u SET u.xpSemanal = 0")
    void resetarXpSemanal();

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE Usuario u SET u.diasOfensiva = CASE WHEN (u.dataUltimaOfensiva = :ontem) THEN u.diasOfensiva + 1 ELSE 1 END, u.dataUltimaOfensiva = :hoje WHERE u.id = :id AND (u.dataUltimaOfensiva IS NULL OR u.dataUltimaOfensiva != :hoje)")
    int atualizarOfensivaSeNecessario(@Param("id") Long id, @Param("hoje") java.time.LocalDate hoje, @Param("ontem") java.time.LocalDate ontem);
}

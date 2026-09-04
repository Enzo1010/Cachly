package br.com.cachly.backend.simulador.desafio;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DesafioConcluidoRepository extends JpaRepository<DesafioConcluido, Long> {
    boolean existsByUsuarioIdAndDesafioId(Long usuarioId, String desafioId);
}

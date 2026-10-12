package br.com.cachly.backend.simulador.desafio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DesafioSimuladorRepository extends JpaRepository<DesafioSimulador, String> {
}

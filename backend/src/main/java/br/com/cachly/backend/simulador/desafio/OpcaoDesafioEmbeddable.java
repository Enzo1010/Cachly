package br.com.cachly.backend.simulador.desafio;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpcaoDesafioEmbeddable {

    @Column(name = "letra", nullable = false)
    private String id; // Represents the letter A, B, C, D

    @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
    private String texto;
}

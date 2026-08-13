package br.com.cachly.backend.simulador;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SimulacaoRequest(
    @NotNull(message = "Tamanho da cache em bytes é obrigatório")
    @Min(value = 1, message = "Tamanho da cache deve ser maior que 0")
    Integer tamanhoCacheBytes,

    @NotNull(message = "Tamanho do bloco em bytes é obrigatório")
    @Min(value = 1, message = "Tamanho do bloco deve ser maior que 0")
    Integer tamanhoBlocoBytes,

    Integer numeroVias,

    @NotNull(message = "Tipo de mapeamento é obrigatório")
    TipoMapeamento mapeamento,

    PoliticaSubstituicao substituicao,

    @NotEmpty(message = "A lista de endereços não pode estar vazia")
    List<Integer> enderecos
) {}

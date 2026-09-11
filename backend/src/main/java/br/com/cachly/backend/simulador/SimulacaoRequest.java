package br.com.cachly.backend.simulador;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SimulacaoRequest(
    @NotNull(message = "Tamanho da cache em bytes é obrigatório")
    @Min(value = 1, message = "Tamanho da cache deve ser maior que 0")
    @Max(value = 1048576, message = "Tamanho da cache não pode exceder 1 MB (1048576 bytes)")
    Integer tamanhoCacheBytes,

    @NotNull(message = "Tamanho do bloco em bytes é obrigatório")
    @Min(value = 1, message = "Tamanho do bloco deve ser maior que 0")
    @Max(value = 65536, message = "Tamanho do bloco não pode exceder 64 KB (65536 bytes)")
    Integer tamanhoBlocoBytes,

    Integer numeroVias,

    @NotNull(message = "Tipo de mapeamento é obrigatório")
    TipoMapeamento mapeamento,

    PoliticaSubstituicao substituicao,

    @NotEmpty(message = "A lista de endereços não pode estar vazia")
    @Size(max = 200, message = "A lista não pode exceder 200 endereços")
    List<@NotNull @PositiveOrZero(message = "Endereço de memória não pode ser negativo") Integer> enderecos
) {}

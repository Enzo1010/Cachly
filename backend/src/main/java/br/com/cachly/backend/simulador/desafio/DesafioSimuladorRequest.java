package br.com.cachly.backend.simulador.desafio;

import br.com.cachly.backend.questao.DificuldadeQuestao;
import br.com.cachly.backend.simulador.SimulacaoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record DesafioSimuladorRequest(
        @NotBlank(message = "O ID (identificador único) é obrigatório")
        String id,

        @NotBlank(message = "O título é obrigatório")
        String titulo,

        @NotBlank(message = "A descrição é obrigatória")
        String descricao,

        @NotNull(message = "A dificuldade é obrigatória")
        DificuldadeQuestao dificuldade,

        @NotNull(message = "A recompensa em XP é obrigatória")
        @Positive(message = "A recompensa em XP deve ser maior que zero")
        Integer xpRecompensa,

        @NotNull(message = "A configuração do simulador é obrigatória")
        @Valid
        SimulacaoRequest configuracao,

        @NotBlank(message = "A pergunta é obrigatória")
        String pergunta,

        @NotEmpty(message = "O desafio deve possuir ao menos uma opção")
        @Size(max = 10, message = "O desafio não pode possuir mais de 10 opções")
        List<@Valid OpcaoDesafioRequest> opcoes,

        @NotBlank(message = "A opção correta (letra) é obrigatória")
        String opcaoCorretaId,

        @NotBlank(message = "A dica é obrigatória")
        String dica,

        @NotBlank(message = "A explicação é obrigatória")
        String explicacao
) {}

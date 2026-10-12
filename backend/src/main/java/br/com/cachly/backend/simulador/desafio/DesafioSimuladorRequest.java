package br.com.cachly.backend.simulador.desafio;

import br.com.cachly.backend.questao.DificuldadeQuestao;
import br.com.cachly.backend.simulador.SimulacaoRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
        Integer xpRecompensa,

        @NotNull(message = "A configuração do simulador é obrigatória")
        SimulacaoRequest configuracao,

        @NotBlank(message = "A pergunta é obrigatória")
        String pergunta,

        @NotNull(message = "As opções são obrigatórias")
        List<OpcaoDesafioRequest> opcoes,

        @NotBlank(message = "A opção correta (letra) é obrigatória")
        String opcaoCorretaId,

        @NotBlank(message = "A dica é obrigatória")
        String dica,

        @NotBlank(message = "A explicação é obrigatória")
        String explicacao
) {}

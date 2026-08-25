package br.com.cachly.backend.resposta;

import br.com.cachly.backend.questao.Questao;
import br.com.cachly.backend.usuario.Usuario;

public record QuestaoRespondidaEvent(
        Usuario usuario,
        Questao questao,
        boolean correta,
        boolean primeiraVezCorreta
) {
}

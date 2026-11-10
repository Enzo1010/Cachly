package br.com.cachly.backend.simulador;

import br.com.cachly.backend.questao.DificuldadeQuestao;
import br.com.cachly.backend.simulador.desafio.*;
import java.util.List;

public final class DesafiosTeste {
    private DesafiosTeste() {}

    public static List<DesafioSimulador> todos() {
        return List.of(criar("cold-miss", 30, "A"), criar("thrashing-direto", 50, "B"),
                criar("solucao-associativa", 60, "C"), criar("lru-vs-fifo", 80, "D"));
    }

    private static DesafioSimulador criar(String id, int xp, String correta) {
        var desafio = new DesafioSimulador();
        desafio.setId(id);
        desafio.setTitulo("Desafio de teste");
        desafio.setDescricao("Descrição de teste");
        desafio.setDificuldade(DificuldadeQuestao.FACIL);
        desafio.setXpRecompensa(xp);
        desafio.setConfiguracao(new SimulacaoRequest(64, 16, null, TipoMapeamento.DIRETO, null, List.of(0, 16, 32, 48)));
        desafio.setPergunta("Quantos misses?");
        desafio.setOpcoes(List.of(new OpcaoDesafioEmbeddable("A", "Quatro misses"), new OpcaoDesafioEmbeddable("B", "Dois misses")));
        desafio.setOpcaoCorretaId(correta);
        desafio.setDica("Cache inicialmente vazia");
        desafio.setExplicacao("Os primeiros acessos são compulsórios");
        return desafio;
    }
}

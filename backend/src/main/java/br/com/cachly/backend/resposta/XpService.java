package br.com.cachly.backend.resposta;

import br.com.cachly.backend.questao.Questao;
import org.springframework.stereotype.Service;

@Service
public class XpService {

    public int calcularXpGanho(Questao questao) {
        int multiplicador = switch (questao.getDificuldade()) {
            case FACIL -> 1;
            case MEDIO -> 2;
            case DIFICIL -> 3;
        };
        return questao.getXpBase() * multiplicador;
    }

    /**
     * Calcula o nível correspondente ao XP total acumulado.
     * Complexidade O(1) utilizando a fórmula de Bhaskara.
     *
     * Limiares:
     *   Nível 1: 0 XP
     *   Nível 2: 100 XP
     *   Nível 3: 250 XP
     *   Nível 4: 450 XP
     *   Nível N: 25 * (N-1) * (N+2) XP
     */
    public int calcularNivel(int xpTotal) {
        if (xpTotal <= 0) {
            return 1;
        }
        // Resolvendo a equação quadrática: 25x² + 75x - XP = 0
        return (int) ((-75.0 + Math.sqrt(5625.0 + 100.0 * xpTotal)) / 50.0) + 1;
    }

    /**
     * Retorna o nome do título correspondente ao nível.
     * Níveis acima de 10 recebem o título máximo.
     */
    public String nomeDoNivel(int nivel) {
        return switch (nivel) {
            case 1 -> "Estagiário";
            case 2 -> "Dev Junior";
            case 3 -> "Dev";
            case 4 -> "Dev Pleno";
            case 5 -> "Dev Sênior";
            case 6 -> "Tech Lead";
            case 7 -> "Software Architect";
            case 8 -> "Principal Engineer";
            case 9 -> "CTO";
            default -> "sudo";
        };
    }
}

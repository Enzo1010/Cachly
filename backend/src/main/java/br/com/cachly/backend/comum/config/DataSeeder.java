package br.com.cachly.backend.comum.config;

import br.com.cachly.backend.categoria.Categoria;
import br.com.cachly.backend.categoria.CategoriaRepository;
import br.com.cachly.backend.questao.Questao;
import br.com.cachly.backend.questao.QuestaoRepository;
import br.com.cachly.backend.alternativa.Alternativa;
import br.com.cachly.backend.resposta.TentativaQuestao;
import br.com.cachly.backend.resposta.TentativaQuestaoRepository;
import br.com.cachly.backend.usuario.PerfilUsuario;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final QuestaoRepository questaoRepository;
    private final TentativaQuestaoRepository tentativaQuestaoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (usuarioRepository.findByEmailIgnoreCase("aluno@teste.com").isPresent()) {
            return; // Dados de teste já populados
        }

        // 1. Criar Usuário de Teste
        Usuario aluno = new Usuario();
        aluno.setNome("Aluno de Teste");
        aluno.setEmail("aluno@teste.com");
        aluno.setSenhaHash(passwordEncoder.encode("senha123"));
        aluno.setPerfil(PerfilUsuario.ALUNO);
        aluno.setAtivo(true);
        aluno.setNivel(3);
        aluno.setXpTotal(450);
        aluno.setDiasOfensiva(5);
        aluno.setDataUltimaOfensiva(java.time.LocalDate.now());
        aluno = usuarioRepository.save(aluno);

        // 2. Criar Categorias de Arquitetura
        Categoria catDireto = salvarCategoria("Mapeamento Direto", "Mapeamento onde cada bloco da principal vai para uma linha específica da cache.");
        Categoria catTotalmente = salvarCategoria("Totalmente Associativo", "Mapeamento onde um bloco pode ir para qualquer linha da cache.");
        Categoria catConjunto = salvarCategoria("Associativo por Conjunto", "Mapeamento híbrido (N-way set associative).");
        Categoria catSubstituicao = salvarCategoria("Políticas de Substituição", "Algoritmos como LRU, FIFO e LFU.");

        // 3. Criar Questões com Alternativas
        Questao q1 = criarQuestao(catDireto, "No mapeamento direto, qual é a principal desvantagem?", 10);
        Questao q2 = criarQuestao(catTotalmente, "No totalmente associativo, como ocorre a busca pelo bloco?", 15);
        Questao q3 = criarQuestao(catConjunto, "O que define a quantidade de blocos por conjunto?", 20);
        Questao q4 = criarQuestao(catSubstituicao, "O que o algoritmo LRU faz?", 10);
        Questao q5 = criarQuestao(catDireto, "Como o índice da cache é calculado no mapeamento direto?", 15);

        // 4. Criar Histórico de Tentativas para gerar o Gráfico de Radar
        // - Mapeamento Direto: Ótimo desempenho (Acertou 4 de 5 tentativas) = 80%
        gerarTentativas(aluno, q1, 5, 4);
        gerarTentativas(aluno, q5, 5, 4);

        // - Totalmente Associativo: Desempenho Mediano (Acertou 3 de 5) = 60%
        gerarTentativas(aluno, q2, 5, 3);

        // - Substituição: Desempenho Bom (Acertou 4 de 5) = 80%
        gerarTentativas(aluno, q4, 5, 4);

        // - Associativo por Conjunto: PÉSSIMO (Acertou 1 de 5) = 20% -> ESSA SERÁ A RECOMENDAÇÃO NA TELA!
        gerarTentativas(aluno, q3, 5, 1);
        
        System.out.println("✅ [DataSeeder] Dados de teste pedagógicos injetados com sucesso! Use: aluno@teste.com / senha123");
    }

    private Categoria salvarCategoria(String nome, String descricao) {
        Categoria c = new Categoria();
        c.setNome(nome);
        c.setDescricao(descricao);
        return categoriaRepository.save(c);
    }

    private Questao criarQuestao(Categoria categoria, String enunciado, int xp) {
        Questao q = new Questao();
        q.setCategoria(categoria);
        q.setEnunciado(enunciado);
        q.setDificuldade(br.com.cachly.backend.questao.DificuldadeQuestao.FACIL);
        q.setXpBase(xp);
        q.setExplicacao("Explicação padrão para: " + enunciado);

        Alternativa correta = new Alternativa();
        correta.setTexto("Resposta correta");
        correta.setCorreta(true);
        correta.setOrdem((short) 1);
        correta.setQuestao(q);

        Alternativa errada1 = new Alternativa();
        errada1.setTexto("Resposta errada A");
        errada1.setCorreta(false);
        errada1.setOrdem((short) 2);
        errada1.setQuestao(q);
        
        Alternativa errada2 = new Alternativa();
        errada2.setTexto("Resposta errada B");
        errada2.setCorreta(false);
        errada2.setOrdem((short) 3);
        errada2.setQuestao(q);

        q.getAlternativas().addAll(List.of(correta, errada1, errada2));
        
        return questaoRepository.save(q);
    }

    private void gerarTentativas(Usuario usuario, Questao questao, int total, int acertos) {
        Alternativa correta = questao.getAlternativas().stream().filter(Alternativa::getCorreta).findFirst().orElseThrow();
        Alternativa errada = questao.getAlternativas().stream().filter(a -> !a.getCorreta()).findFirst().orElseThrow();

        OffsetDateTime tempoBase = OffsetDateTime.now().minusDays(10);
        Random r = new Random();

        for (int i = 0; i < total; i++) {
            TentativaQuestao t = new TentativaQuestao();
            t.setUsuario(usuario);
            t.setQuestao(questao);
            
            boolean acertouNesta = i < acertos;
            t.setAlternativa(acertouNesta ? correta : errada);
            t.setCorreta(acertouNesta);
            t.setXpConcedido(acertouNesta ? questao.getXpBase() : 0);
            
            // Distribuir no tempo para o gráfico histórico ficar bonito
            t.setRespondidaEm(tempoBase.plusDays(i).plusHours(r.nextInt(12)));
            
            tentativaQuestaoRepository.save(t);
        }
    }
}

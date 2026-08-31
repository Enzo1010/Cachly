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
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final QuestaoRepository questaoRepository;
    private final TentativaQuestaoRepository tentativaQuestaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final jakarta.persistence.EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Limpar dados de teste antigos ("Cat Teste") e o próprio seeder anterior para garantir frescor
        limparDadosAntigos();

        if (usuarioRepository.findByEmailIgnoreCase("aluno@teste.com").isPresent()) {
            return; // Já populado nesta nova versão
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

        // 3. Criar Questões com Alternativas REAIS
        Questao q1 = criarQuestaoReal(catDireto, 
            "Qual é a principal característica do mapeamento direto de cache?", 10,
            "No mapeamento direto, a função modular (Endereço de Bloco % Número de Linhas) determina uma única posição exata onde o bloco pode residir.",
            "Cada bloco da memória principal é mapeado para exatamente uma linha (ou bloco) específica na cache.",
            "Qualquer bloco da memória principal pode ser armazenado em qualquer linha da cache.",
            "A memória cache é dividida em conjuntos, e um bloco pode ocupar qualquer linha dentro de um conjunto.");

        Questao q2 = criarQuestaoReal(catDireto, 
            "Como o endereço de memória é dividido no mapeamento direto?", 15,
            "O endereço é dividido em três partes: o Deslocamento (Offset) para o byte, o Índice (Index) para a linha específica, e a Tag para verificação.",
            "Tag, Índice (Index) e Deslocamento (Offset).",
            "Tag e Deslocamento (Offset) apenas.",
            "Tag, Conjunto (Set) e Deslocamento (Offset).");

        Questao q3 = criarQuestaoReal(catTotalmente, 
            "Qual é a maior vantagem do mapeamento totalmente associativo em comparação com o mapeamento direto?", 15,
            "No totalmente associativo, conflitos de linha não ocorrem até que a cache esteja 100% cheia, pois qualquer bloco pode ser colocado em qualquer lugar.",
            "Reduz significativamente a taxa de misses por conflito, pois qualquer bloco pode ser alocado em qualquer linha.",
            "É muito mais barato de implementar em hardware, pois requer apenas um comparador.",
            "O tempo de busca do dado (hit time) é menor devido à ausência do campo de Índice.");

        Questao q4 = criarQuestaoReal(catConjunto, 
            "O mapeamento associativo por conjunto (N-way) é um meio-termo entre quais mapeamentos?", 20,
            "O associativo por conjunto equilibra o baixo custo do mapeamento direto com a alta flexibilidade do totalmente associativo.",
            "Direto e Totalmente Associativo.",
            "Direto e Paginação.",
            "Totalmente Associativo e Segmentação.");

        Questao q5 = criarQuestaoReal(catSubstituicao, 
            "Qual o critério utilizado pela política de substituição LRU (Least Recently Used)?", 10,
            "O LRU (Menos Recentemente Usado) explora a localidade temporal, removendo o bloco que está há mais tempo sem ser referenciado.",
            "Substitui o bloco que não foi acessado há mais tempo.",
            "Substitui o bloco mais antigo presente na cache, independentemente de quando foi acessado.",
            "Substitui o bloco que teve o menor número total de acessos desde que foi carregado.");

        // 4. Criar Histórico de Tentativas para gerar o Gráfico de Radar
        gerarTentativas(aluno, q1, 5, 4);
        gerarTentativas(aluno, q2, 5, 4);
        gerarTentativas(aluno, q3, 5, 3);
        gerarTentativas(aluno, q5, 5, 4);
        gerarTentativas(aluno, q4, 5, 1);
        
        System.out.println("✅ [DataSeeder] Dados de teste pedagógicos injetados com sucesso! Use: aluno@teste.com / senha123");
    }

    private void limparDadosAntigos() {
        // Deletar dependências para evitar constraint violations
        entityManager.createNativeQuery("DELETE FROM tentativas_questao WHERE questao_id IN (SELECT id FROM questoes WHERE categoria_id IN (SELECT id FROM categorias WHERE nome LIKE 'Cat Teste%' OR nome IN ('Mapeamento Direto', 'Totalmente Associativo', 'Associativo por Conjunto', 'Políticas de Substituição')))").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM alternativas WHERE questao_id IN (SELECT id FROM questoes WHERE categoria_id IN (SELECT id FROM categorias WHERE nome LIKE 'Cat Teste%' OR nome IN ('Mapeamento Direto', 'Totalmente Associativo', 'Associativo por Conjunto', 'Políticas de Substituição')))").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM questoes WHERE categoria_id IN (SELECT id FROM categorias WHERE nome LIKE 'Cat Teste%' OR nome IN ('Mapeamento Direto', 'Totalmente Associativo', 'Associativo por Conjunto', 'Políticas de Substituição'))").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM categorias WHERE nome LIKE 'Cat Teste%' OR nome IN ('Mapeamento Direto', 'Totalmente Associativo', 'Associativo por Conjunto', 'Políticas de Substituição')").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tentativas_questao WHERE usuario_id IN (SELECT id FROM usuarios WHERE email = 'aluno@teste.com')").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM usuarios WHERE email = 'aluno@teste.com'").executeUpdate();
    }

    private Categoria salvarCategoria(String nome, String descricao) {
        Categoria c = new Categoria();
        c.setNome(nome);
        c.setDescricao(descricao);
        return categoriaRepository.save(c);
    }

    private Questao criarQuestaoReal(Categoria categoria, String enunciado, int xp, String explicacao, String corretaTxt, String errada1Txt, String errada2Txt) {
        Questao q = new Questao();
        q.setCategoria(categoria);
        q.setEnunciado(enunciado);
        q.setDificuldade(br.com.cachly.backend.questao.DificuldadeQuestao.MEDIO);
        q.setXpBase(xp);
        q.setExplicacao(explicacao);

        Alternativa correta = new Alternativa();
        correta.setTexto(corretaTxt);
        correta.setCorreta(true);
        correta.setOrdem((short) 1);
        correta.setQuestao(q);

        Alternativa errada1 = new Alternativa();
        errada1.setTexto(errada1Txt);
        errada1.setCorreta(false);
        errada1.setOrdem((short) 2);
        errada1.setQuestao(q);
        
        Alternativa errada2 = new Alternativa();
        errada2.setTexto(errada2Txt);
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

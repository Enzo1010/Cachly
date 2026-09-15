package br.com.cachly.backend.questao;

import br.com.cachly.backend.alternativa.Alternativa;
import br.com.cachly.backend.categoria.Categoria;
import br.com.cachly.backend.categoria.CategoriaRepository;
import br.com.cachly.backend.resposta.TentativaQuestao;
import br.com.cachly.backend.resposta.TentativaQuestaoRepository;
import br.com.cachly.backend.usuario.PerfilUsuario;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class QuestaoIntegracaoTest {

    @Autowired
    private QuestaoService questaoService;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TentativaQuestaoRepository tentativaQuestaoRepository;

    @Test
    @Transactional
    public void naoDeveLancarDataIntegrityAoEditarQuestaoRespondida() {
        // 1. Criar dados base
        Categoria categoria = new Categoria();
        categoria.setNome("Categoria Teste");
        categoria.setDescricao("Teste");
        categoria.setAtiva(true);
        categoria = categoriaRepository.saveAndFlush(categoria);

        Usuario aluno = new Usuario();
        aluno.setNome("Aluno Teste");
        aluno.setEmail("aluno_integracao_questao@teste.com");
        aluno.setSenhaHash("hash");
        aluno.setPerfil(PerfilUsuario.ALUNO);
        aluno.setAtivo(true);
        aluno.setNivel(1);
        aluno.setXpTotal(0);
        aluno.setDiasOfensiva(0);
        aluno = usuarioRepository.saveAndFlush(aluno);

        // 2. Criar Questão via Service
        QuestaoRequest requestBase = new QuestaoRequest(
                categoria.getId(),
                "Enunciado original",
                "Explicação original",
                DificuldadeQuestao.FACIL,
                10,
                List.of(
                        new br.com.cachly.backend.alternativa.AlternativaRequest(null, "Alt 1", true, (short) 1),
                        new br.com.cachly.backend.alternativa.AlternativaRequest(null, "Alt 2", false, (short) 2)
                )
        );
        QuestaoResponse questaoSalva = questaoService.cadastrar(requestBase);
        Long questaoId = questaoSalva.id();
        
        // Obter alternativas persistidas
        Questao questaoBase = questaoRepository.findById(questaoId).orElseThrow();
        Alternativa alternativaEscolhida = questaoBase.getAlternativas().stream().filter(Alternativa::getCorreta).findFirst().orElseThrow();
        Alternativa alternativaRemovida = questaoBase.getAlternativas().stream().filter(a -> !a.getCorreta()).findFirst().orElseThrow();

        // 3. Simular tentativa de resposta pelo aluno (cria FK em tentativas_questao apontando para a AlternativaEscolhida)
        TentativaQuestao tentativa = new TentativaQuestao();
        tentativa.setUsuario(aluno);
        tentativa.setQuestao(questaoBase);
        tentativa.setAlternativa(alternativaEscolhida);
        tentativa.setCorreta(true);
        tentativa.setXpConcedido(10);
        tentativa.setRespondidaEm(OffsetDateTime.now());
        tentativaQuestaoRepository.saveAndFlush(tentativa);

        // 4. Editar a Questão:
        // - Atualizar a Alt 1 (mantendo id)
        // - Remover a Alt 2 (não enviar no request) -> DEVE causar soft-delete e não erro
        // - Adicionar nova Alt 3 (sem id)
        QuestaoRequest requestEdicao = new QuestaoRequest(
                categoria.getId(),
                "Enunciado Editado",
                "Explicação Editada",
                DificuldadeQuestao.FACIL,
                10,
                List.of(
                        new br.com.cachly.backend.alternativa.AlternativaRequest(alternativaEscolhida.getId(), "Alt 1 Editada", true, (short) 1),
                        new br.com.cachly.backend.alternativa.AlternativaRequest(null, "Alt 3 Nova", false, (short) 3)
                )
        );

        // Se o clear() com orphanRemoval = true estiver presente, isso lançará DataIntegrityViolationException
        // Se a correção do merge inteligente estiver funcionando, passará normalmente.
        assertDoesNotThrow(() -> {
            questaoService.atualizar(questaoId, requestEdicao);
        });

        // 5. Verificar estado pós-edição
        Questao questaoEditada = questaoRepository.findById(questaoId).orElseThrow();
        assertEquals(3, questaoEditada.getAlternativas().size(), "Deve conter 3 alternativas no banco (2 ativas, 1 inativa)");
        
        long ativas = questaoEditada.getAlternativas().stream().filter(Alternativa::getAtiva).count();
        assertEquals(2, ativas, "Apenas as enviadas no update devem estar ativas");
        
        Alternativa altDesativada = questaoEditada.getAlternativas().stream()
                .filter(a -> a.getId().equals(alternativaRemovida.getId())).findFirst().orElseThrow();
        assertFalse(altDesativada.getAtiva(), "Alternativa removida do request deve sofrer soft-delete");
    }
}

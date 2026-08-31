package br.com.cachly.backend.resposta;

import br.com.cachly.backend.alternativa.Alternativa;
import br.com.cachly.backend.alternativa.AlternativaRepository;
import br.com.cachly.backend.categoria.Categoria;
import br.com.cachly.backend.categoria.CategoriaRepository;
import br.com.cachly.backend.questao.DificuldadeQuestao;
import br.com.cachly.backend.questao.Questao;
import br.com.cachly.backend.questao.QuestaoRepository;
import br.com.cachly.backend.usuario.PerfilUsuario;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TentativaQuestaoRepositoryTest {

    @Autowired
    private TentativaQuestaoRepository tentativaQuestaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private AlternativaRepository alternativaRepository;

    @Test
    void deveGarantirOrdemDeterministicaComTimestampsIdenticos() {
        Usuario usuario = new Usuario();
        usuario.setNome("Tester");
        usuario.setEmail("tester." + java.util.UUID.randomUUID() + "@exemplo.com");
        usuario.setSenhaHash("hash");
        usuario.setPerfil(PerfilUsuario.ALUNO);
        usuario.setNivel(1);
        usuario.setAtivo(true);
        usuario = usuarioRepository.save(usuario);

        Categoria categoria = new Categoria();
        categoria.setNome("Cat Teste " + java.util.UUID.randomUUID());
        categoria.setDescricao("Desc");
        categoria = categoriaRepository.save(categoria);

        Questao questao = new Questao();
        questao.setEnunciado("Enunciado");
        questao.setExplicacao("Explicacao");
        questao.setCategoria(categoria);
        questao.setDificuldade(DificuldadeQuestao.FACIL);
        questao.setXpBase(10);
        questao = questaoRepository.save(questao);

        Alternativa alt = new Alternativa();
        alt.setTexto("Alt");
        alt.setCorreta(true);
        alt.setQuestao(questao);
        alt.setOrdem((short) 1);
        alt = alternativaRepository.save(alt);

        OffsetDateTime exatoMomento = OffsetDateTime.parse("2025-01-01T10:00:00Z");

        TentativaQuestao t1 = new TentativaQuestao();
        t1.setUsuario(usuario);
        t1.setQuestao(questao);
        t1.setAlternativa(alt);
        t1.setCorreta(true);
        t1.setXpConcedido(10);
        t1.setRespondidaEm(exatoMomento);
        t1 = tentativaQuestaoRepository.save(t1);

        TentativaQuestao t2 = new TentativaQuestao();
        t2.setUsuario(usuario);
        t2.setQuestao(questao);
        t2.setAlternativa(alt);
        t2.setCorreta(true);
        t2.setXpConcedido(10);
        t2.setRespondidaEm(exatoMomento);
        t2 = tentativaQuestaoRepository.save(t2);

        Page<TentativaQuestao> page = tentativaQuestaoRepository.findByUsuarioIdOrderByRespondidaEmDesc(
                usuario.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
        
        TentativaQuestao primeiro = page.getContent().get(0);
        TentativaQuestao segundo = page.getContent().get(1);

        assertThat(primeiro.getId()).isGreaterThan(segundo.getId());
        assertThat(primeiro.getId()).isEqualTo(t2.getId());
        assertThat(segundo.getId()).isEqualTo(t1.getId());
    }
}

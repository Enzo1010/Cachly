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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RespostaIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private AlternativaRepository alternativaRepository;

    @Autowired
    private TentativaQuestaoRepository tentativaQuestaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deveRegistrarRespostaIntegradaComAutenticacaoJWT() throws Exception {
        // 1. Criar Categoria
        Categoria categoria = new Categoria();
        categoria.setNome("Sistemas Digitais " + UUID.randomUUID());
        categoria.setDescricao("Descrição da categoria");
        categoria.setAtiva(true);
        categoriaRepository.saveAndFlush(categoria);

        // 2. Criar Questão
        Questao questao = new Questao();
        questao.setCategoria(categoria);
        questao.setEnunciado("Qual a porta lógica da operação AND?");
        questao.setExplicacao("A porta AND resulta em 1 apenas quando todas as entradas forem 1.");
        questao.setDificuldade(DificuldadeQuestao.FACIL);
        questao.setXpBase(10);
        questao.setAtiva(true);
        questaoRepository.saveAndFlush(questao);

        // 3. Criar Alternativas
        Alternativa altCorreta = new Alternativa();
        altCorreta.setQuestao(questao);
        altCorreta.setTexto("Porta E (AND)");
        altCorreta.setCorreta(true);
        altCorreta.setOrdem((short) 1);
        altCorreta.setAtiva(true);

        Alternativa altIncorreta = new Alternativa();
        altIncorreta.setQuestao(questao);
        altIncorreta.setTexto("Porta OU (OR)");
        altIncorreta.setCorreta(false);
        altIncorreta.setOrdem((short) 2);
        altIncorreta.setAtiva(true);

        alternativaRepository.saveAllAndFlush(List.of(altCorreta, altIncorreta));

        // 4. Criar Aluno
        String email = "aluno.resposta.%s@cachly.local".formatted(UUID.randomUUID());
        Usuario aluno = new Usuario();
        aluno.setNome("Aluno Resposta");
        aluno.setEmail(email);
        aluno.setSenhaHash(passwordEncoder.encode("senha123"));
        aluno.setPerfil(PerfilUsuario.ALUNO);
        aluno.setXpTotal(0);
        aluno.setNivel(1);
        aluno.setAtivo(true);
        usuarioRepository.saveAndFlush(aluno);

        // 5. Efetuar Login
        String loginResponseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "senha": "senha123"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponseBody).get("token").asText();

        // 6. Enviar resposta para a questão
        mockMvc.perform(post("/api/questoes/%d/respostas".formatted(questao.getId()))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "alternativaId": %d
                                }
                                """.formatted(altCorreta.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correta").value(true))
                .andExpect(jsonPath("$.explicacao").value("A porta AND resulta em 1 apenas quando todas as entradas forem 1."))
                .andExpect(jsonPath("$.xpConcedido").value(10))
                .andExpect(jsonPath("$.nivelAtual").value(1))
                .andExpect(jsonPath("$.xpTotal").value(10));

        // 7. Verificar banco de dados
        assertEquals(1, tentativaQuestaoRepository.count());
        TentativaQuestao tentativa = tentativaQuestaoRepository.findAll().get(0);
        assertEquals(aluno.getId(), tentativa.getUsuario().getId());
        assertEquals(questao.getId(), tentativa.getQuestao().getId());
        assertEquals(altCorreta.getId(), tentativa.getAlternativa().getId());
        assertTrue(tentativa.getCorreta());
        assertEquals(10, tentativa.getXpConcedido());
        assertNotNull(tentativa.getRespondidaEm());

        // 8. Verificar que xpTotal e nivel do aluno foram atualizados
        Usuario alunoAtualizado = usuarioRepository.findById(aluno.getId()).orElseThrow();
        assertEquals(10, alunoAtualizado.getXpTotal());
        assertEquals(1, alunoAtualizado.getNivel());
    }
}

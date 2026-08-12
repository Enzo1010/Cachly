package br.com.cachly.backend.usuario.aluno;

import br.com.cachly.backend.comum.erro.ConflitoDeDadosException;

import org.junit.jupiter.api.Test;
import br.com.cachly.backend.usuario.UsuarioService;
import br.com.cachly.backend.usuario.PerfilUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlunoController.class)
@ActiveProfiles("test")
class AlunoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private AlunoDesempenhoService alunoDesempenhoService;

    @Test
    void deveCadastrarAlunoERetornarStatusCriadoSemExporSenha() throws Exception {
        when(usuarioService.cadastrarAluno(any(AlunoCadastroRequest.class)))
                .thenReturn(criarResponse());

        mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Silva",
                                  "email": "ana.silva@exemplo.com",
                                  "senha": "senha-segura",
                                  "perfil": "ADMINISTRADOR"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.email").value("ana.silva@exemplo.com"))
                .andExpect(jsonPath("$.perfil").value("ALUNO"))
                .andExpect(jsonPath("$.xpTotal").value(0))
                .andExpect(jsonPath("$.nivel").value(1))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void deveRecusarNomeEmailESenhaInvalidos() throws Exception {
        mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "",
                                  "email": "email-invalido",
                                  "senha": "curta"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    @Test
    void deveRecusarNomeEEmailAcimaDosLimitesDoBanco() throws Exception {
        String nomeLongo = "a".repeat(101);
        String emailLongo = "a".repeat(141) + "@exemplo.com";

        mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "email": "%s",
                                  "senha": "senha-segura"
                                }
                                """.formatted(nomeLongo, emailLongo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.email").exists());
    }

    @Test
    void deveRetornarConflitoQuandoEmailJaEstiverEmUso() throws Exception {
        when(usuarioService.cadastrarAluno(any(AlunoCadastroRequest.class)))
                .thenThrow(new ConflitoDeDadosException(
                        "Já existe um usuário com esse e-mail"
                ));

        mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Silva",
                                  "email": "ana.silva@exemplo.com",
                                  "senha": "senha-segura"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem")
                        .value("Já existe um usuário com esse e-mail"));
    }

    private AlunoResponse criarResponse() {
        OffsetDateTime agora = OffsetDateTime.now();
        return new AlunoResponse(
                1L,
                "Ana Silva",
                "ana.silva@exemplo.com",
                PerfilUsuario.ALUNO,
                0,
                1,
                "Estagiário",
                true,
                agora,
                agora
        );
    }
}

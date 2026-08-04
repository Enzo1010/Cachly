package br.com.questly.backend.usuario;

import br.com.questly.backend.comum.erro.ConflitoDeDadosException;
import br.com.questly.backend.comum.erro.CredenciaisInvalidasException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder codificadorSenha;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deveCadastrarAlunoComSenhaProtegidaEValoresIniciais() {
        AlunoCadastroRequest request = new AlunoCadastroRequest(
                "  Ana Silva  ",
                "  Ana.Silva@Exemplo.com  ",
                "senha-segura"
        );

        when(usuarioRepository.existsByEmailIgnoreCase("ana.silva@exemplo.com"))
                .thenReturn(false);
        when(codificadorSenha.encode("senha-segura"))
                .thenReturn("hash-bcrypt");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacao -> {
                    Usuario usuario = invocacao.getArgument(0);
                    OffsetDateTime agora = OffsetDateTime.now();
                    usuario.setId(1L);
                    usuario.setCriadoEm(agora);
                    usuario.setAtualizadoEm(agora);
                    return usuario;
                });

        AlunoResponse response = usuarioService.cadastrarAluno(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario usuarioSalvo = captor.getValue();

        assertEquals("Ana Silva", usuarioSalvo.getNome());
        assertEquals("ana.silva@exemplo.com", usuarioSalvo.getEmail());
        assertEquals("hash-bcrypt", usuarioSalvo.getSenhaHash());
        assertNotEquals(request.senha(), usuarioSalvo.getSenhaHash());
        assertEquals(PerfilUsuario.ALUNO, usuarioSalvo.getPerfil());
        assertEquals(0, usuarioSalvo.getXpTotal());
        assertEquals(1, usuarioSalvo.getNivel());
        assertTrue(usuarioSalvo.getAtivo());

        assertEquals(1L, response.id());
        assertEquals("Ana Silva", response.nome());
        assertEquals("ana.silva@exemplo.com", response.email());
        assertEquals(PerfilUsuario.ALUNO, response.perfil());
        assertEquals(0, response.xpTotal());
        assertEquals(1, response.nivel());
        assertTrue(response.ativo());
    }

    @Test
    void deveRecusarCadastroQuandoEmailJaEstiverEmUsoIgnorandoMaiusculas() {
        AlunoCadastroRequest request = new AlunoCadastroRequest(
                "Ana Silva",
                "ANA.SILVA@EXEMPLO.COM",
                "senha-segura"
        );
        when(usuarioRepository.existsByEmailIgnoreCase("ana.silva@exemplo.com"))
                .thenReturn(true);

        assertThrows(
                ConflitoDeDadosException.class,
                () -> usuarioService.cadastrarAluno(request)
        );

        verify(codificadorSenha, never()).encode(any(String.class));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void deveAutenticarUsuarioAtivoComCredenciaisValidas() {
        Usuario usuario = criarUsuarioAtivo();
        when(usuarioRepository.findByEmailIgnoreCase("ana.silva@exemplo.com"))
                .thenReturn(Optional.of(usuario));
        when(codificadorSenha.matches("senha-segura", "hash-bcrypt"))
                .thenReturn(true);

        UsuarioAutenticadoResponse response = usuarioService.autenticar(
                new AutenticacaoRequest(
                        "  Ana.Silva@Exemplo.com  ",
                        "senha-segura"
                )
        );

        assertEquals(1L, response.id());
        assertEquals("Ana Silva", response.nome());
        assertEquals("ana.silva@exemplo.com", response.email());
        assertEquals(PerfilUsuario.ALUNO, response.perfil());
        assertEquals(0, response.xpTotal());
        assertEquals(1, response.nivel());
    }

    @Test
    void deveRecusarAutenticacaoQuandoEmailNaoExistir() {
        when(usuarioRepository.findByEmailIgnoreCase("inexistente@exemplo.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                CredenciaisInvalidasException.class,
                () -> usuarioService.autenticar(new AutenticacaoRequest(
                        "inexistente@exemplo.com",
                        "senha-segura"
                ))
        );

        verify(codificadorSenha, never()).matches(any(String.class), any(String.class));
    }

    @Test
    void deveRecusarAutenticacaoQuandoSenhaEstiverIncorreta() {
        Usuario usuario = criarUsuarioAtivo();
        when(usuarioRepository.findByEmailIgnoreCase("ana.silva@exemplo.com"))
                .thenReturn(Optional.of(usuario));
        when(codificadorSenha.matches("senha-incorreta", "hash-bcrypt"))
                .thenReturn(false);

        assertThrows(
                CredenciaisInvalidasException.class,
                () -> usuarioService.autenticar(new AutenticacaoRequest(
                        "ana.silva@exemplo.com",
                        "senha-incorreta"
                ))
        );
    }

    @Test
    void deveRecusarAutenticacaoQuandoUsuarioEstiverInativo() {
        Usuario usuario = criarUsuarioAtivo();
        usuario.setAtivo(false);
        when(usuarioRepository.findByEmailIgnoreCase("ana.silva@exemplo.com"))
                .thenReturn(Optional.of(usuario));

        assertThrows(
                CredenciaisInvalidasException.class,
                () -> usuarioService.autenticar(new AutenticacaoRequest(
                        "ana.silva@exemplo.com",
                        "senha-segura"
                ))
        );

        verify(codificadorSenha, never()).matches(any(String.class), any(String.class));
    }

    private Usuario criarUsuarioAtivo() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Ana Silva");
        usuario.setEmail("ana.silva@exemplo.com");
        usuario.setSenhaHash("hash-bcrypt");
        usuario.setPerfil(PerfilUsuario.ALUNO);
        usuario.setXpTotal(0);
        usuario.setNivel(1);
        usuario.setAtivo(true);
        return usuario;
    }
}

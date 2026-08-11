package br.com.questly.backend.usuario.aluno;

import br.com.questly.backend.usuario.PerfilUsuario;
import br.com.questly.backend.usuario.Usuario;
import br.com.questly.backend.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<RankingResponse> listarRanking(Pageable pageable) {
        Page<Usuario> usuarios = usuarioRepository.findByPerfil(PerfilUsuario.ALUNO, pageable);
        
        return usuarios.map(usuario -> {
            // A posição é baseada no offset da página + index do elemento atual
            int posicao = (pageable.getPageNumber() * pageable.getPageSize()) + usuarios.getContent().indexOf(usuario) + 1;
            
            return new RankingResponse(
                    posicao,
                    usuario.getNome(),
                    usuario.getNivel(),
                    usuario.getXpTotal()
            );
        });
    }
}

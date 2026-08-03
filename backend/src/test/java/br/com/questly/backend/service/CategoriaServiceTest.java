package br.com.questly.backend.service;

import br.com.questly.backend.dto.CategoriaRequest;
import br.com.questly.backend.dto.CategoriaResponse;
import br.com.questly.backend.entity.Categoria;
import br.com.questly.backend.exception.ConflitoDeDadosException;
import br.com.questly.backend.exception.RecursoNaoEncontradoException;
import br.com.questly.backend.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void deveCadastrarCategoriaQuandoNomeEstiverDisponivel() {
        CategoriaRequest request = new CategoriaRequest("  Álgebra Booleana  ", "   ");

        when(categoriaRepository.existsByNomeIgnoreCase("Álgebra Booleana"))
                .thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class)))
                .thenAnswer(invocacao -> {
                    Categoria categoria = invocacao.getArgument(0);
                    categoria.setId(1L);
                    categoria.setCriadoEm(OffsetDateTime.now());
                    categoria.setAtualizadoEm(OffsetDateTime.now());
                    return categoria;
                });

        CategoriaResponse response = categoriaService.cadastrar(request);

        ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).save(captor.capture());

        assertEquals(1L, response.id());
        assertEquals("Álgebra Booleana", response.nome());
        assertEquals("Álgebra Booleana", captor.getValue().getNome());
        assertNull(captor.getValue().getDescricao());
    }

    @Test
    void deveRecusarCadastroQuandoNomeJaExistir() {
        CategoriaRequest request = new CategoriaRequest("Portas Lógicas", null);
        when(categoriaRepository.existsByNomeIgnoreCase("Portas Lógicas"))
                .thenReturn(true);

        assertThrows(
                ConflitoDeDadosException.class,
                () -> categoriaService.cadastrar(request)
        );

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void deveListarCategoriasAtivas() {
        Categoria primeira = criarCategoria(1L, "Álgebra Booleana", true);
        Categoria segunda = criarCategoria(2L, "Portas Lógicas", true);
        when(categoriaRepository.findAllByAtivaTrueOrderByNomeAsc())
                .thenReturn(List.of(primeira, segunda));

        List<CategoriaResponse> resultado = categoriaService.listarAtivas();

        assertEquals(2, resultado.size());
        assertEquals("Álgebra Booleana", resultado.get(0).nome());
        assertEquals("Portas Lógicas", resultado.get(1).nome());
    }

    @Test
    void deveBuscarCategoriaPorId() {
        Categoria categoria = criarCategoria(1L, "Circuitos Digitais", true);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        CategoriaResponse response = categoriaService.buscarPorId(1L);

        assertEquals(1L, response.id());
        assertEquals("Circuitos Digitais", response.nome());
    }

    @Test
    void deveInformarQuandoCategoriaNaoForEncontrada() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> categoriaService.buscarPorId(99L)
        );
    }

    @Test
    void deveAtualizarCategoriaQuandoNomeEstiverDisponivel() {
        Categoria categoria = criarCategoria(1L, "Circuitos", true);
        CategoriaRequest request = new CategoriaRequest(
                "  Circuitos Digitais  ",
                "  Conteúdo atualizado  "
        );

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNomeIgnoreCaseAndIdNot("Circuitos Digitais", 1L))
                .thenReturn(false);
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        CategoriaResponse response = categoriaService.atualizar(1L, request);

        assertEquals("Circuitos Digitais", response.nome());
        assertEquals("Conteúdo atualizado", response.descricao());
        verify(categoriaRepository).save(categoria);
    }

    @Test
    void deveRecusarAtualizacaoQuandoNomeJaPertencerAOutraCategoria() {
        Categoria categoria = criarCategoria(1L, "Circuitos", true);
        CategoriaRequest request = new CategoriaRequest("Portas Lógicas", null);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNomeIgnoreCaseAndIdNot("Portas Lógicas", 1L))
                .thenReturn(true);

        assertThrows(
                ConflitoDeDadosException.class,
                () -> categoriaService.atualizar(1L, request)
        );

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void deveDesativarCategoriaSemExcluiLa() {
        Categoria categoria = criarCategoria(1L, "Pipeline", true);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        CategoriaResponse response = categoriaService.desativar(1L);

        assertFalse(response.ativa());
        assertFalse(categoria.getAtiva());
        verify(categoriaRepository).save(categoria);
        verify(categoriaRepository, never()).delete(any(Categoria.class));
    }

    private Categoria criarCategoria(Long id, String nome, boolean ativa) {
        OffsetDateTime agora = OffsetDateTime.now();
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNome(nome);
        categoria.setDescricao("Descrição da categoria");
        categoria.setAtiva(ativa);
        categoria.setCriadoEm(agora);
        categoria.setAtualizadoEm(agora);
        return categoria;
    }
}

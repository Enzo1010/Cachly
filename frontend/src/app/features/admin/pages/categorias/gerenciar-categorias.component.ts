import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';

import { AdminApiService } from '../../services/admin-api.service';
import { CategoriaAdminRequest, CategoriaAdminResponse } from '../../models/admin.model';

@Component({
  selector: 'app-gerenciar-categorias',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './gerenciar-categorias.component.html',
  styleUrl: './gerenciar-categorias.component.scss',
})
export class GerenciarCategoriasComponent {
  private readonly adminApi = inject(AdminApiService);
  private readonly notificacoes = inject(MessageService);

  protected readonly carregando = signal(true);
  protected readonly salvando = signal(false);
  protected readonly modalAberto = signal(false);
  protected readonly categoriaEmEdicao = signal<CategoriaAdminResponse | null>(null);

  protected readonly categorias = signal<CategoriaAdminResponse[]>([]);
  protected readonly filtroBusca = signal('');

  protected readonly formulario = new FormGroup({
    nome: new FormControl<string>('', [
      Validators.required,
      Validators.minLength(2),
      Validators.maxLength(100),
    ]),
    descricao: new FormControl<string>(''),
  });

  protected readonly categoriasFiltradas = computed(() => {
    const lista = this.categorias();
    const busca = this.filtroBusca().toLowerCase().trim();

    if (!busca) return lista;

    return lista.filter(
      (c) =>
        c.nome.toLowerCase().includes(busca) ||
        (c.descricao && c.descricao.toLowerCase().includes(busca))
    );
  });

  constructor() {
    this.carregarCategorias();
  }

  protected carregarCategorias(): void {
    this.carregando.set(true);
    this.adminApi.listarCategorias().subscribe({
      next: (dados) => {
        this.categorias.set(dados);
        this.carregando.set(false);
      },
      error: () => {
        this.notificacoes.add({
          severity: 'error',
          summary: 'Erro',
          detail: 'Não foi possível carregar as categorias.',
        });
        this.carregando.set(false);
      },
    });
  }

  protected abrirModalNovaCategoria(): void {
    this.categoriaEmEdicao.set(null);
    this.formulario.reset({
      nome: '',
      descricao: '',
    });
    this.modalAberto.set(true);
  }

  protected abrirModalEditar(c: CategoriaAdminResponse): void {
    this.categoriaEmEdicao.set(c);
    this.formulario.patchValue({
      nome: c.nome,
      descricao: c.descricao ?? '',
    });
    this.modalAberto.set(true);
  }

  protected fecharModal(): void {
    this.modalAberto.set(false);
    this.categoriaEmEdicao.set(null);
  }

  protected salvar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      this.notificacoes.add({
        severity: 'warn',
        summary: 'Formulário inválido',
        detail: 'Preencha o nome da categoria corretamente.',
      });
      return;
    }

    const { nome, descricao } = this.formulario.getRawValue();
    const payload: CategoriaAdminRequest = {
      nome: nome!,
      descricao: descricao ? descricao.trim() : null,
    };

    this.salvando.set(true);
    const edicao = this.categoriaEmEdicao();

    if (edicao) {
      this.adminApi.atualizarCategoria(edicao.id, payload).subscribe({
        next: () => {
          this.notificacoes.add({
            severity: 'success',
            summary: 'Atualizado',
            detail: 'Categoria atualizada com sucesso!',
          });
          this.salvando.set(false);
          this.fecharModal();
          this.carregarCategorias();
        },
        error: (erro) => {
          this.salvando.set(false);
          const msg = erro?.error?.mensagem || 'Falha ao atualizar categoria.';
          this.notificacoes.add({
            severity: 'error',
            summary: 'Erro',
            detail: msg,
          });
        },
      });
    } else {
      this.adminApi.cadastrarCategoria(payload).subscribe({
        next: () => {
          this.notificacoes.add({
            severity: 'success',
            summary: 'Cadastrado',
            detail: 'Categoria criada com sucesso!',
          });
          this.salvando.set(false);
          this.fecharModal();
          this.carregarCategorias();
        },
        error: (erro) => {
          this.salvando.set(false);
          const msg = erro?.error?.mensagem || 'Falha ao cadastrar categoria.';
          this.notificacoes.add({
            severity: 'error',
            summary: 'Erro',
            detail: msg,
          });
        },
      });
    }
  }

  protected alternarStatus(c: CategoriaAdminResponse): void {
    const acao$ = c.ativa
      ? this.adminApi.desativarCategoria(c.id)
      : this.adminApi.ativarCategoria(c.id);

    acao$.subscribe({
      next: (atualizada) => {
        this.notificacoes.add({
          severity: 'info',
          summary: atualizada.ativa ? 'Ativada' : 'Desativada',
          detail: `Categoria "${c.nome}" foi ${atualizada.ativa ? 'ativada' : 'desativada'}.`,
        });
        this.carregarCategorias();
      },
      error: () => {
        this.notificacoes.add({
          severity: 'error',
          summary: 'Erro',
          detail: 'Não foi possível alterar o status da categoria.',
        });
      },
    });
  }
}

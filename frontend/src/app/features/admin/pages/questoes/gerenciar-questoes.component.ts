import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';

import { AdminApiService } from '../../services/admin-api.service';
import {
  CategoriaAdminResponse,
  DificuldadeQuestao,
  QuestaoAdminRequest,
  QuestaoAdminResponse,
} from '../../models/admin.model';

@Component({
  selector: 'app-gerenciar-questoes',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './gerenciar-questoes.component.html',
  styleUrl: './gerenciar-questoes.component.scss',
})
export class GerenciarQuestoesComponent {
  private readonly adminApi = inject(AdminApiService);
  private readonly notificacoes = inject(MessageService);

  protected readonly carregando = signal(true);
  protected readonly salvando = signal(false);
  protected readonly modalAberto = signal(false);
  protected readonly questaoEmEdicao = signal<QuestaoAdminResponse | null>(null);

  protected readonly questoes = signal<QuestaoAdminResponse[]>([]);
  protected readonly categorias = signal<CategoriaAdminResponse[]>([]);

  // Filtros
  protected readonly filtroBusca = signal('');
  protected readonly filtroCategoria = signal<number | null>(null);
  protected readonly filtroDificuldade = signal<string>('TODAS');
  protected readonly filtroStatus = signal<string>('TODAS');

  // Formulário Reativo
  protected readonly formulario = new FormGroup({
    categoriaId: new FormControl<number | null>(null, [Validators.required]),
    enunciado: new FormControl<string>('', [Validators.required, Validators.minLength(5)]),
    explicacao: new FormControl<string>('', [Validators.required, Validators.minLength(5)]),
    dificuldade: new FormControl<DificuldadeQuestao>('FACIL', [Validators.required]),
    xpBase: new FormControl<number>(10, [Validators.required, Validators.min(1)]),
    alternativas: new FormArray([
      this.criarGrupoAlternativa(1, true),
      this.criarGrupoAlternativa(2, false),
      this.criarGrupoAlternativa(3, false),
      this.criarGrupoAlternativa(4, false),
    ]),
  });

  protected readonly questoesFiltradas = computed(() => {
    let lista = this.questoes();
    const busca = this.filtroBusca().toLowerCase().trim();
    const catId = this.filtroCategoria();
    const dif = this.filtroDificuldade();
    const st = this.filtroStatus();

    if (busca) {
      lista = lista.filter(
        (q) =>
          q.enunciado.toLowerCase().includes(busca) ||
          q.categoriaNome.toLowerCase().includes(busca)
      );
    }

    if (catId !== null) {
      lista = lista.filter((q) => q.categoriaId === catId);
    }

    if (dif !== 'TODAS') {
      lista = lista.filter((q) => q.dificuldade === dif);
    }

    if (st === 'ATIVAS') {
      lista = lista.filter((q) => q.ativa);
    } else if (st === 'INATIVAS') {
      lista = lista.filter((q) => !q.ativa);
    }

    return lista;
  });

  get alternativasFormArray(): FormArray {
    return this.formulario.get('alternativas') as FormArray;
  }

  constructor() {
    this.carregarDados();
  }

  private criarGrupoAlternativa(ordem: number, correta: boolean): FormGroup {
    return new FormGroup({
      id: new FormControl<number | null>(null),
      texto: new FormControl<string>('', [Validators.required]),
      correta: new FormControl<boolean>(correta),
      ordem: new FormControl<number>(ordem),
    });
  }

  protected marcarCorreta(indiceSelecionado: number): void {
    this.alternativasFormArray.controls.forEach((ctrl, idx) => {
      ctrl.get('correta')?.setValue(idx === indiceSelecionado);
    });
  }

  protected carregarDados(): void {
    this.carregando.set(true);
    this.adminApi.listarQuestoes().subscribe({
      next: (q) => {
        this.questoes.set(q);
        this.adminApi.listarCategoriasAtivas().subscribe({
          next: (c) => {
            this.categorias.set(c);
            this.carregando.set(false);
          },
          error: () => this.carregando.set(false),
        });
      },
      error: () => {
        this.notificacoes.add({
          severity: 'error',
          summary: 'Erro',
          detail: 'Não foi possível carregar as questões.',
        });
        this.carregando.set(false);
      },
    });
  }

  protected abrirModalNovaQuestao(): void {
    this.questaoEmEdicao.set(null);
    this.formulario.reset({
      categoriaId: this.categorias().length > 0 ? this.categorias()[0].id : null,
      enunciado: '',
      explicacao: '',
      dificuldade: 'FACIL',
      xpBase: 10,
    });

    this.alternativasFormArray.clear();
    for (let i = 1; i <= 4; i++) {
      this.alternativasFormArray.push(this.criarGrupoAlternativa(i, i === 1));
    }

    this.modalAberto.set(true);
  }

  protected abrirModalEditar(q: QuestaoAdminResponse): void {
    this.questaoEmEdicao.set(q);
    this.formulario.patchValue({
      categoriaId: q.categoriaId,
      enunciado: q.enunciado,
      explicacao: q.explicacao,
      dificuldade: q.dificuldade,
      xpBase: q.xpBase,
    });

    this.alternativasFormArray.clear();
    const sortedAlts = [...q.alternativas].sort((a, b) => a.ordem - b.ordem);
    sortedAlts.forEach((alt, idx) => {
      const g = new FormGroup({
        id: new FormControl<number | null>(alt.id),
        texto: new FormControl<string>(alt.texto, [Validators.required]),
        correta: new FormControl<boolean>(alt.correta),
        ordem: new FormControl<number>(alt.ordem || idx + 1),
      });
      this.alternativasFormArray.push(g);
    });

    this.modalAberto.set(true);
  }

  protected fecharModal(): void {
    this.modalAberto.set(false);
    this.questaoEmEdicao.set(null);
  }

  protected salvar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      this.notificacoes.add({
        severity: 'warn',
        summary: 'Formulário incompleto',
        detail: 'Preencha todos os campos obrigatórios e alternativas.',
      });
      return;
    }

    const val = this.formulario.getRawValue();
    const alternativas = val.alternativas as Array<{
      id?: number | null;
      texto: string;
      correta: boolean;
      ordem: number;
    }>;

    const corretas = alternativas.filter((a) => a.correta).length;
    if (corretas !== 1) {
      this.notificacoes.add({
        severity: 'warn',
        summary: 'Atenção',
        detail: 'Exatamente UMA alternativa deve ser marcada como correta.',
      });
      return;
    }

    const payload: QuestaoAdminRequest = {
      categoriaId: val.categoriaId!,
      enunciado: val.enunciado!,
      explicacao: val.explicacao!,
      dificuldade: val.dificuldade!,
      xpBase: val.xpBase!,
      alternativas,
    };

    this.salvando.set(true);
    const edicao = this.questaoEmEdicao();

    if (edicao) {
      this.adminApi.atualizarQuestao(edicao.id, payload).subscribe({
        next: () => {
          this.notificacoes.add({
            severity: 'success',
            summary: 'Atualizado',
            detail: 'Questão atualizada com sucesso!',
          });
          this.salvando.set(false);
          this.fecharModal();
          this.carregarDados();
        },
        error: () => {
          this.salvando.set(false);
          this.notificacoes.add({
            severity: 'error',
            summary: 'Erro',
            detail: 'Falha ao atualizar questão.',
          });
        },
      });
    } else {
      this.adminApi.cadastrarQuestao(payload).subscribe({
        next: () => {
          this.notificacoes.add({
            severity: 'success',
            summary: 'Cadastrado',
            detail: 'Questão criada com sucesso!',
          });
          this.salvando.set(false);
          this.fecharModal();
          this.carregarDados();
        },
        error: () => {
          this.salvando.set(false);
          this.notificacoes.add({
            severity: 'error',
            summary: 'Erro',
            detail: 'Falha ao cadastrar questão.',
          });
        },
      });
    }
  }

  protected alternarStatus(q: QuestaoAdminResponse): void {
    const acao$ = q.ativa
      ? this.adminApi.desativarQuestao(q.id)
      : this.adminApi.ativarQuestao(q.id);

    acao$.subscribe({
      next: (atualizada) => {
        this.notificacoes.add({
          severity: 'info',
          summary: atualizada.ativa ? 'Ativada' : 'Desativada',
          detail: `Questão #${q.id} foi ${atualizada.ativa ? 'ativada' : 'desativada'}.`,
        });
        this.carregarDados();
      },
      error: () => {
        this.notificacoes.add({
          severity: 'error',
          summary: 'Erro',
          detail: 'Não foi possível alterar o status da questão.',
        });
      },
    });
  }
}

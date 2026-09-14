import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';

import { AdminApiService } from '../../services/admin-api.service';
import { CategoriaAdminResponse, MetricasAdmin, QuestaoAdminResponse } from '../../models/admin.model';

@Component({
  selector: 'app-visao-geral-admin',
  imports: [RouterLink, DatePipe],
  templateUrl: './visao-geral-admin.component.html',
  styleUrl: './visao-geral-admin.component.scss',
})
export class VisaoGeralAdminComponent {
  private readonly adminApi = inject(AdminApiService);

  protected readonly carregando = signal(true);
  protected readonly questoes = signal<QuestaoAdminResponse[]>([]);
  protected readonly categorias = signal<CategoriaAdminResponse[]>([]);

  protected readonly metricas = computed<MetricasAdmin>(() => {
    const qList = this.questoes();
    const cList = this.categorias();

    const ativas = qList.filter((q) => q.ativa).length;
    const inativas = qList.length - ativas;
    const catAtivas = cList.filter((c) => c.ativa).length;
    const catInativas = cList.length - catAtivas;

    const faceis = qList.filter((q) => q.dificuldade === 'FACIL').length;
    const medias = qList.filter((q) => q.dificuldade === 'MEDIO').length;
    const dificeis = qList.filter((q) => q.dificuldade === 'DIFICIL').length;

    return {
      totalQuestoes: qList.length,
      questoesAtivas: ativas,
      questoesInativas: inativas,
      totalCategorias: cList.length,
      categoriasAtivas: catAtivas,
      categoriasInativas: catInativas,
      questoesFaceis: faceis,
      questoesMedias: medias,
      questoesDificeis: dificeis,
    };
  });

  protected readonly questoesRecentes = computed(() => {
    return [...this.questoes()].reverse().slice(0, 5);
  });

  constructor() {
    this.carregarDados();
  }

  private carregarDados(): void {
    this.carregando.set(true);
    this.adminApi.listarQuestoes().subscribe({
      next: (q) => {
        this.questoes.set(q);
        this.adminApi.listarCategorias().subscribe({
          next: (c) => {
            this.categorias.set(c);
            this.carregando.set(false);
          },
          error: () => this.carregando.set(false),
        });
      },
      error: () => this.carregando.set(false),
    });
  }
}

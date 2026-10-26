import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { ButtonDirective } from 'primeng/button';

import {
  DesempenhoCategoriaResponse,
  DesempenhoResponse,
} from '../../../../shared/models/desempenho.model';
import { DesempenhoApiService } from '../../../../shared/services/desempenho-api.service';

const MINIMO_TENTATIVAS_PARA_RECOMENDAR = 3;

@Component({
  selector: 'app-desempenho',
  standalone: true,
  imports: [RouterLink, ButtonDirective],
  templateUrl: './desempenho.component.html',
  styleUrl: './desempenho.component.scss',
})
export class DesempenhoComponent implements OnInit {
  private readonly desempenhoApi = inject(DesempenhoApiService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly desempenho = signal<DesempenhoResponse | null>(null);
  protected readonly carregando = signal(true);
  protected readonly erro = signal(false);

  protected readonly categorias = computed(() =>
    [...(this.desempenho()?.estatisticasPorCategoria ?? [])].sort(
      (a, b) => a.taxaAcerto - b.taxaAcerto || b.totalTentativas - a.totalTentativas,
    ),
  );

  protected readonly categoriaParaRevisar = computed<DesempenhoCategoriaResponse | null>(
    () =>
      this.categorias().find(
        (categoria) =>
          categoria.totalTentativas >= MINIMO_TENTATIVAS_PARA_RECOMENDAR &&
          categoria.acertos < categoria.totalTentativas,
      ) ?? null,
  );

  ngOnInit(): void {
    this.carregarDesempenho();
  }

  protected carregarDesempenho(): void {
    this.carregando.set(true);
    this.erro.set(false);

    this.desempenhoApi
      .obterDesempenho()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (resposta) => {
          this.desempenho.set(resposta);
          this.carregando.set(false);
        },
        error: () => {
          this.erro.set(true);
          this.carregando.set(false);
        },
      });
  }
}

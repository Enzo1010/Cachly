import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { ButtonDirective } from 'primeng/button';
import { ChartModule } from 'primeng/chart';
import type { ChartOptions } from 'chart.js';

import { SessaoService } from '../../../../core/autenticacao/sessao.service';
import { DesempenhoCategoriaResponse } from '../../../../shared/models/desempenho.model';
import { DesempenhoApiService } from '../../../../shared/services/desempenho-api.service';

const MINIMO_TENTATIVAS_PARA_RECOMENDAR = 3;

interface IndicadorVisaoGeral {
  readonly destaque: string;
  readonly descricao: string;
  readonly icone: string;
  readonly variacao?: string;
}

@Component({
  selector: 'app-visao-geral',
  imports: [ButtonDirective, ChartModule, RouterLink],
  templateUrl: './visao-geral.component.html',
  styleUrl: './visao-geral.component.scss',
})
export class VisaoGeralComponent {
  protected readonly sessao = inject(SessaoService);
  private readonly desempenhoApi = inject(DesempenhoApiService);
  private readonly router = inject(Router);

  protected readonly desempenho = toSignal(this.desempenhoApi.obterDesempenho());

  protected readonly indicadores = computed<readonly IndicadorVisaoGeral[]>(() => {
    const d = this.desempenho();

    return [
      {
        destaque: d ? d.acertos.toString() : '0',
        descricao: 'Questões Corretas',
        icone: 'pi pi-check',
      },
      {
        destaque: d ? `${d.taxaAcerto}%` : '0%',
        descricao: 'Taxa de Acerto Geral',
        icone: 'pi pi-bullseye',
      },
      {
        destaque: `Nível ${this.sessao.usuario()?.nivel ?? 1}`,
        descricao: this.sessao.usuario()?.nomeNivel ?? 'Nível Atual',
        icone: 'pi pi-crown',
      },
    ];
  });

  protected readonly piorCategoria = computed<DesempenhoCategoriaResponse | null>(() => {
    const categorias = this.desempenho()?.estatisticasPorCategoria ?? [];
    const elegiveis = categorias
      .filter(
        (c) =>
          c.totalTentativas >= MINIMO_TENTATIVAS_PARA_RECOMENDAR &&
          c.acertos < c.totalTentativas,
      )
      .sort((a, b) => a.taxaAcerto - b.taxaAcerto || b.totalTentativas - a.totalTentativas);

    return elegiveis[0] ?? null;
  });

  protected readonly dadosGraficoRadar = computed(() => {
    const categorias = this.desempenho()?.estatisticasPorCategoria ?? [];
    return {
      labels: categorias.map((c) => c.categoriaNome),
      datasets: [
        {
          label: 'Taxa de Acerto (%)',
          backgroundColor: 'rgba(59, 130, 246, 0.2)',
          borderColor: 'rgba(59, 130, 246, 1)',
          pointBackgroundColor: 'rgba(59, 130, 246, 1)',
          pointBorderColor: '#fff',
          pointHoverBackgroundColor: '#fff',
          pointHoverBorderColor: 'rgba(59, 130, 246, 1)',
          data: categorias.map((c) => c.taxaAcerto),
        },
      ],
    };
  });

  protected readonly opcoesGraficoRadar: ChartOptions<'radar'> = {
    plugins: {
      legend: {
        labels: {
          color: '#495057',
        },
      },
    },
    scales: {
      r: {
        pointLabels: {
          color: '#495057',
        },
        grid: {
          color: '#ebedef',
        },
        angleLines: {
          color: '#ebedef',
        },
        suggestedMin: 0,
        suggestedMax: 100,
      },
    },
  };

  protected irParaSimulador(): void {
    void this.router.navigateByUrl('/simulador');
  }
}

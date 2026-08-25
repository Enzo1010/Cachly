import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { ButtonDirective } from 'primeng/button';
import { ChartModule } from 'primeng/chart';

import { SessaoService } from '../../../../core/autenticacao/sessao.service';
import { DashboardService } from '../../services/dashboard.service';

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
  private readonly dashboardService = inject(DashboardService);
  private readonly router = inject(Router);

  protected readonly desempenho = toSignal(this.dashboardService.obterDesempenho());

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
        descricao: 'Sua Posição',
        icone: 'pi pi-crown',
        variacao: 'pi pi-arrow-right',
      },
    ];
  });

  protected readonly piorCategoria = computed(() => {
    const categorias = this.desempenho()?.estatisticasPorCategoria ?? [];
    if (categorias.length === 0) return null;
    
    // Encontra a categoria com menor taxa de acerto, ignorando quem tem 0 tentativas
    const categoriasValidas = categorias.filter(c => c.totalTentativas > 0);
    if (categoriasValidas.length === 0) return null;

    return categoriasValidas.reduce((menor, atual) =>
      atual.taxaAcerto < menor.taxaAcerto ? atual : menor
    );
  });

  protected readonly dadosGraficoRadar = computed(() => {
    const categorias = this.desempenho()?.estatisticasPorCategoria ?? [];
    return {
      labels: categorias.map(c => c.categoriaNome),
      datasets: [
        {
          label: 'Taxa de Acerto (%)',
          backgroundColor: 'rgba(59, 130, 246, 0.2)',
          borderColor: 'rgba(59, 130, 246, 1)',
          pointBackgroundColor: 'rgba(59, 130, 246, 1)',
          pointBorderColor: '#fff',
          pointHoverBackgroundColor: '#fff',
          pointHoverBorderColor: 'rgba(59, 130, 246, 1)',
          data: categorias.map(c => c.taxaAcerto)
        }
      ]
    };
  });

  protected readonly opcoesGraficoRadar = {
    plugins: {
      legend: {
        labels: {
          color: '#495057'
        }
      }
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
          color: '#ebedef'
        },
        suggestedMin: 0,
        suggestedMax: 100
      }
    }
  };

  protected irParaSimulador(): void {
    void this.router.navigateByUrl('/simulador');
  }
}


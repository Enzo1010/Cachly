import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ButtonDirective } from 'primeng/button';

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
  imports: [ButtonDirective],
  templateUrl: './visao-geral.component.html',
  styleUrl: './visao-geral.component.scss',
})
export class VisaoGeralComponent {
  protected readonly sessao = inject(SessaoService);
  private readonly dashboardService = inject(DashboardService);

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
    
    // Encontra a categoria com menor taxa de acerto
    return categorias.reduce((menor, atual) =>
      atual.taxaAcerto < menor.taxaAcerto ? atual : menor
    );
  });
}


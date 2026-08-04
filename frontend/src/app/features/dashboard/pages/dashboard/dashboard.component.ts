import { Component, inject } from '@angular/core';
import { ButtonDirective } from 'primeng/button';

import { SessaoService } from '../../../../core/autenticacao/sessao.service';

interface IndicadorDashboard {
  readonly destaque: string;
  readonly descricao: string;
  readonly icone: string;
  readonly variacao?: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [ButtonDirective],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {
  protected readonly sessao = inject(SessaoService);

  protected readonly indicadores: readonly IndicadorDashboard[] = [
    {
      destaque: '351',
      descricao: 'Questões Corretas',
      icone: 'pi pi-check',
    },
    {
      destaque: '82%',
      descricao: 'Taxa de Acerto Geral',
      icone: 'pi pi-bullseye',
    },
    {
      destaque: 'Liga Prata',
      descricao: 'Ver Posição',
      icone: 'pi pi-medal',
      variacao: 'pi pi-arrow-right',
    },
  ];
}

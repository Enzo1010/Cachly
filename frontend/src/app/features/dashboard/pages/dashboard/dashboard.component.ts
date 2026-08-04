import { Component } from '@angular/core';
import { ButtonDirective } from 'primeng/button';

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

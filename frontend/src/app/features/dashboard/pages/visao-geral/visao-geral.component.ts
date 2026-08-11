import { Component, inject } from '@angular/core';
import { ButtonDirective } from 'primeng/button';

import { SessaoService } from '../../../../core/autenticacao/sessao.service';

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

  protected readonly indicadores: readonly IndicadorVisaoGeral[] = [
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
      icone: 'pi pi-crown',
      variacao: 'pi pi-arrow-right',
    },
  ];
}

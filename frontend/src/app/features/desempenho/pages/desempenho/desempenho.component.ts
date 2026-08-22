import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';

import { DesempenhoCategoriaResponse } from '../../models/desempenho.model';
import { StatusTopico } from '../../models/progresso-modulo.model';
import { DesempenhoService } from '../../services/desempenho.service';
import { ProgressoModuloService } from '../../services/progresso-modulo.service';

type NivelDesempenho = 'alta' | 'media' | 'baixa';

@Component({
  selector: 'app-desempenho',
  templateUrl: './desempenho.component.html',
  styleUrl: './desempenho.component.scss',
})
export class DesempenhoComponent {
  private readonly desempenhoService = inject(DesempenhoService);
  private readonly progressoModuloService = inject(ProgressoModuloService);

  protected readonly desempenho = toSignal(this.desempenhoService.obterDesempenho());
  protected readonly categorias = computed(() => this.desempenho()?.estatisticasPorCategoria ?? []);

  private readonly categoriaMaisFraca = computed<DesempenhoCategoriaResponse | null>(() => {
    const categorias = this.categorias();

    if (categorias.length === 0) {
      return null;
    }

    return categorias.reduce((maisFraca, atual) =>
      atual.taxaAcerto < maisFraca.taxaAcerto ? atual : maisFraca,
    );
  });

  protected readonly moduloAtual = computed(() => {
    const categoria = this.categoriaMaisFraca();
    return categoria ? this.progressoModuloService.obterProgresso(categoria) : null;
  });

  protected nivelDesempenho(taxaAcerto: number): NivelDesempenho {
    if (taxaAcerto >= 80) {
      return 'alta';
    }

    if (taxaAcerto >= 60) {
      return 'media';
    }

    return 'baixa';
  }

  protected iconeTopico(status: StatusTopico): string {
    switch (status) {
      case 'concluido':
        return 'pi-check-circle';
      case 'atual':
        return 'pi-circle-fill';
      default:
        return 'pi-circle';
    }
  }
}

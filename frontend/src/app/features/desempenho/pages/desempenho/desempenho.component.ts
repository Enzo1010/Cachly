import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';

import { DesempenhoCategoriaResponse } from '../../../../shared/models/desempenho.model';
import { StatusTopico } from '../../models/progresso-modulo.model';
import { DesempenhoApiService } from '../../../../shared/services/desempenho-api.service';
import { ProgressoModuloService } from '../../services/progresso-modulo.service';
import { SessaoService } from '../../../../core/autenticacao/sessao.service';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';

type NivelDesempenho = 'alta' | 'media' | 'baixa';

@Component({
  selector: 'app-desempenho',
  standalone: true,
  imports: [CommonModule, RouterLink, ButtonModule],
  templateUrl: './desempenho.component.html',
  styleUrl: './desempenho.component.scss',
})
export class DesempenhoComponent {
  private readonly desempenhoApi = inject(DesempenhoApiService);
  private readonly progressoModuloService = inject(ProgressoModuloService);
  private readonly sessaoService = inject(SessaoService);

  protected readonly usuario = this.sessaoService.usuario;
  protected readonly desempenho = toSignal(this.desempenhoApi.obterDesempenho());
  
  protected readonly categorias = computed(() => {
    const cats = this.desempenho()?.estatisticasPorCategoria ?? [];
    // Ordenar categorias por taxa de acerto (do maior para o menor)
    return [...cats].sort((a, b) => b.taxaAcerto - a.taxaAcerto);
  });

  protected readonly categoriaMaisFraca = computed<DesempenhoCategoriaResponse | null>(() => {
    const cats = this.categorias();
    if (cats.length === 0) return null;
    return cats[cats.length - 1];
  });

  protected readonly categoriaMaisForte = computed<DesempenhoCategoriaResponse | null>(() => {
    const cats = this.categorias();
    if (cats.length === 0) return null;
    return cats[0];
  });

  protected readonly moduloAtual = computed(() => {
    const categoria = this.categoriaMaisFraca();
    return categoria ? this.progressoModuloService.obterProgresso(categoria) : null;
  });

  protected nivelDesempenho(taxaAcerto: number): NivelDesempenho {
    if (taxaAcerto >= 80) return 'alta';
    if (taxaAcerto >= 60) return 'media';
    return 'baixa';
  }

  protected iconeTopico(status: StatusTopico): string {
    switch (status) {
      case 'concluido': return 'pi-check-circle';
      case 'atual': return 'pi-clock';
      default: return 'pi-circle';
    }
  }

  protected obterMensagemIncentivo(): string {
    const taxa = this.desempenho()?.taxaAcerto ?? 0;
    if (taxa >= 80) return "Excelente precisão! Você está dominando os conteúdos.";
    if (taxa >= 60) return "Bom trabalho! Continue praticando para lapidar seus conhecimentos.";
    return "A consistência é a chave! Revise os conteúdos e logo você verá sua taxa subir.";
  }
}

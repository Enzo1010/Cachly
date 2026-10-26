import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EstudarStateService } from '../../services/estudar-state.service';
import { EstudarListaComponent } from '../../components/estudar-lista/estudar-lista.component';
import { EstudarResolucaoComponent } from '../../components/estudar-resolucao/estudar-resolucao.component';
import { EstudarResumoComponent } from '../../components/estudar-resumo/estudar-resumo.component';

@Component({
  selector: 'app-estudar-page',
  standalone: true,
  imports: [EstudarListaComponent, EstudarResolucaoComponent, EstudarResumoComponent],
  providers: [EstudarStateService],
  template: `
    <div class="estudar-container">
      @if (state.telaAtual() === 'LISTA') {
        <app-estudar-lista />
      } @else if (state.telaAtual() === 'RESOLUCAO') {
        <app-estudar-resolucao />
      } @else if (state.telaAtual() === 'RESUMO') {
        <app-estudar-resumo />
      }
    </div>
  `,
  styles: [
    `
      .estudar-container {
        padding: 1rem;
        max-width: 1000px;
        margin: 0 auto;
      }
    `,
  ],
})
export class EstudarPageComponent implements OnInit {
  readonly state = inject(EstudarStateService);
  private readonly rota = inject(ActivatedRoute);

  ngOnInit(): void {
    this.state.carregarCategorias();
    const categoriaId = Number(this.rota.snapshot.queryParamMap.get('categoriaId'));

    if (Number.isInteger(categoriaId) && categoriaId > 0) {
      this.state.aplicarFiltroCategoria(categoriaId);
    } else {
      this.state.carregarQuestoes();
    }
  }
}

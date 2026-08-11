import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-pagina-titulo',
  templateUrl: './pagina-titulo.component.html',
  styleUrl: './pagina-titulo.component.scss',
})
export class PaginaTituloComponent {
  private readonly rotaAtiva = inject(ActivatedRoute);

  protected readonly titulo = this.obterTitulo();

  private obterTitulo(): string {
    const titulo = this.rotaAtiva.snapshot.data['titulo'];

    return typeof titulo === 'string' ? titulo : 'Página';
  }
}

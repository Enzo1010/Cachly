import { Routes } from '@angular/router';

export const ROTAS_DESEMPENHO: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('../../shared/components/pagina-titulo/pagina-titulo.component').then(
        (componente) => componente.PaginaTituloComponent,
      ),
    data: { titulo: 'Desempenho' },
    title: 'Desempenho | Questly',
  },
];

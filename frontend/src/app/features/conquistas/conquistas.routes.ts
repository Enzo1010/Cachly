import { Routes } from '@angular/router';

export const ROTAS_CONQUISTAS: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('../../shared/components/pagina-titulo/pagina-titulo.component').then(
        (componente) => componente.PaginaTituloComponent,
      ),
    data: { titulo: 'Conquistas' },
    title: 'Conquistas | Questly',
  },
];

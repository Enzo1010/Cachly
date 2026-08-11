import { Routes } from '@angular/router';

export const ROTAS_LIGA_SEMANAL: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('../../shared/components/pagina-titulo/pagina-titulo.component').then(
        (componente) => componente.PaginaTituloComponent,
      ),
    data: { titulo: 'Liga Semanal' },
    title: 'Liga Semanal | Cachly',
  },
];

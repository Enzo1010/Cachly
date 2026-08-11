import { Routes } from '@angular/router';

export const ROTAS_ESTUDAR: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('../../shared/components/pagina-titulo/pagina-titulo.component').then(
        (componente) => componente.PaginaTituloComponent,
      ),
    data: { titulo: 'Estudar' },
    title: 'Estudar | Questly',
  },
];

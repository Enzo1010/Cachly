import { Routes } from '@angular/router';

export const ROTAS_DASHBOARD: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/visao-geral/visao-geral.component').then(
        (componente) => componente.VisaoGeralComponent,
      ),
    data: { titulo: 'Visao Geral' },
    title: 'Visao Geral | Questly',
  },
];

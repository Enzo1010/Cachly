import { Routes } from '@angular/router';

export const ROTAS_ESTUDAR: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/estudar-page/estudar-page.component').then(
        (m) => m.EstudarPageComponent,
      ),
    data: { titulo: 'Estudar' },
    title: 'Estudar | Cachly',
  },
];

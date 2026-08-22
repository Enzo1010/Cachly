import { Routes } from '@angular/router';

export const ROTAS_DESEMPENHO: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/desempenho/desempenho.component').then(
        (componente) => componente.DesempenhoComponent,
      ),
    data: { titulo: 'Meu Desempenho' },
    title: 'Desempenho | Cachly',
  },
];

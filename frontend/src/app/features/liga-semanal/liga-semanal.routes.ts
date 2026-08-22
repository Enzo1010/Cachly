import { Routes } from '@angular/router';

export const ROTAS_LIGA_SEMANAL: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/liga-semanal/liga-semanal.component').then(
        (componente) => componente.LigaSemanalComponent,
      ),
    data: { titulo: 'Competição Semanal' },
    title: 'Liga Semanal | Cachly',
  },
];

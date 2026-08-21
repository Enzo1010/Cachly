import { Routes } from '@angular/router';

export const ROTAS_CONQUISTAS: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/conquistas/conquistas.component').then(
        (componente) => componente.ConquistasComponent,
      ),
    data: { titulo: 'Minhas Conquistas' },
    title: 'Conquistas | Cachly',
  },
];

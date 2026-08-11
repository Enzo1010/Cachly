import { Routes } from '@angular/router';

export const ROTAS_PERFIL: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/perfil/perfil.component').then((componente) => componente.PerfilComponent),
    title: 'Perfil | Questly',
  },
];

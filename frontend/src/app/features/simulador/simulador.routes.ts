import { Routes } from '@angular/router';

export const ROTAS_SIMULADOR: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/simulador/simulador.component').then(c => c.SimuladorPageComponent),
    title: 'Simulador de Cache | Cachly',
    data: { titulo: 'Simulador' }
  },
];

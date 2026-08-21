import { Routes } from '@angular/router';
import { SimuladorPageComponent } from './pages/simulador/simulador.component';

export const ROTAS_SIMULADOR: Routes = [
  {
    path: '',
    component: SimuladorPageComponent,
    title: 'Simulador de Cache | Cachly',
  },
];

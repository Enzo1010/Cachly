import { Routes } from '@angular/router';

export const ROTAS_AUTENTICACAO: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login.component').then((componente) => componente.LoginComponent),
    title: 'Entrar | Questly',
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login',
  },
];

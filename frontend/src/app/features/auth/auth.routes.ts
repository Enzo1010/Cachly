import { Routes } from '@angular/router';

export const ROTAS_LOGIN: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/login/login.component').then((componente) => componente.LoginComponent),
    title: 'Entrar | Cachly',
  },
];

export const ROTAS_CADASTRO: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/cadastro/cadastro.component').then(
        (componente) => componente.CadastroComponent,
      ),
    title: 'Criar conta | Cachly',
  },
];

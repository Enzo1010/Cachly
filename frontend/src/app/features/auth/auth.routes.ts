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

export const ROTAS_ESQUECI_SENHA: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/esqueci-senha/esqueci-senha.component').then(
        (componente) => componente.EsqueciSenhaComponent,
      ),
    title: 'Esqueci minha senha | Cachly',
  },
];

export const ROTAS_RESETAR_SENHA: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/resetar-senha/resetar-senha.component').then(
        (componente) => componente.ResetarSenhaComponent,
      ),
    title: 'Redefinir senha | Cachly',
  },
];

import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadChildren: () =>
      import('./features/auth/auth.routes').then((rotas) => rotas.ROTAS_AUTENTICACAO),
  },
  {
    path: '**',
    redirectTo: 'login',
  },
];

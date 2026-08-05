import { Routes } from '@angular/router';

import { autenticacaoGuard } from './core/autenticacao/autenticacao.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadChildren: () => import('./features/auth/auth.routes').then((rotas) => rotas.ROTAS_LOGIN),
  },
  {
    path: 'cadastro',
    loadChildren: () => import('./features/auth/auth.routes').then((rotas) => rotas.ROTAS_CADASTRO),
  },
  {
    path: '',
    loadComponent: () =>
      import('./core/layout/layout-principal.component').then(
        (componente) => componente.LayoutPrincipalComponent,
      ),
    canActivate: [autenticacaoGuard],
    children: [
      {
        path: 'dashboard',
        loadChildren: () =>
          import('./features/dashboard/dashboard.routes').then((rotas) => rotas.ROTAS_DASHBOARD),
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard',
      },
    ],
  },
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];

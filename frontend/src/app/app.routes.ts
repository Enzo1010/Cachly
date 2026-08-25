import { Routes } from '@angular/router';

import { autenticacaoGuard } from './core/autenticacao/autenticacao.guard';

import { guestGuard } from './core/autenticacao/guest.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadChildren: () => import('./features/auth/auth.routes').then((rotas) => rotas.ROTAS_LOGIN),
  },
  {
    path: 'cadastro',
    canActivate: [guestGuard],
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
        path: 'estudar',
        loadChildren: () =>
          import('./features/estudar/estudar.routes').then((rotas) => rotas.ROTAS_ESTUDAR),
      },
      {
        path: 'desempenho',
        loadChildren: () =>
          import('./features/desempenho/desempenho.routes').then(
            (rotas) => rotas.ROTAS_DESEMPENHO,
          ),
      },
      {
        path: 'liga-semanal',
        loadChildren: () =>
          import('./features/liga-semanal/liga-semanal.routes').then(
            (rotas) => rotas.ROTAS_LIGA_SEMANAL,
          ),
      },
      {
        path: 'conquistas',
        loadChildren: () =>
          import('./features/conquistas/conquistas.routes').then(
            (rotas) => rotas.ROTAS_CONQUISTAS,
          ),
      },
      {
        path: 'simulador',
        loadChildren: () =>
          import('./features/simulador/simulador.routes').then((rotas) => rotas.ROTAS_SIMULADOR),
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

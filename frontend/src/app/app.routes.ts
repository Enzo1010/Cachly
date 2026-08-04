import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'login',
    loadChildren: () =>
      import('./features/auth/auth.routes').then((rotas) => rotas.ROTAS_AUTENTICACAO),
  },
  {
    path: '',
    loadComponent: () =>
      import('./core/layout/layout-principal.component').then(
        (componente) => componente.LayoutPrincipalComponent,
      ),
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

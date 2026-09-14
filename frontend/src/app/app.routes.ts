import { inject } from '@angular/core';
import { Routes } from '@angular/router';

import { adminGuard } from './core/autenticacao/admin.guard';
import { alunoGuard } from './core/autenticacao/aluno.guard';
import { autenticacaoGuard } from './core/autenticacao/autenticacao.guard';
import { guestGuard } from './core/autenticacao/guest.guard';
import { SessaoService } from './core/autenticacao/sessao.service';

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
        canActivate: [alunoGuard],
        loadChildren: () =>
          import('./features/dashboard/dashboard.routes').then((rotas) => rotas.ROTAS_DASHBOARD),
      },
      {
        path: 'estudar',
        canActivate: [alunoGuard],
        loadChildren: () =>
          import('./features/estudar/estudar.routes').then((rotas) => rotas.ROTAS_ESTUDAR),
      },
      {
        path: 'desempenho',
        canActivate: [alunoGuard],
        loadChildren: () =>
          import('./features/desempenho/desempenho.routes').then(
            (rotas) => rotas.ROTAS_DESEMPENHO,
          ),
      },
      {
        path: 'liga-semanal',
        redirectTo: 'dashboard',
      },
      {
        path: 'conquistas',
        redirectTo: 'dashboard',
      },
      {
        path: 'admin',
        canActivate: [adminGuard],
        loadChildren: () =>
          import('./features/admin/admin.routes').then((rotas) => rotas.ROTAS_ADMIN),
      },
      {
        path: 'simulador',
        loadChildren: () =>
          import('./features/simulador/simulador.routes').then((rotas) => rotas.ROTAS_SIMULADOR),
      },
      {
        path: 'perfil',
        loadChildren: () =>
          import('./features/perfil/perfil.routes').then((rotas) => rotas.ROTAS_PERFIL),
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: () => {
          const sessao = inject(SessaoService);
          return sessao.usuario()?.perfil === 'ADMINISTRADOR' ? 'admin' : 'dashboard';
        },
      },
    ],
  },
  {
    path: '**',
    redirectTo: () => {
      const sessao = inject(SessaoService);
      return sessao.usuario()?.perfil === 'ADMINISTRADOR' ? 'admin' : 'dashboard';
    },
  },
];

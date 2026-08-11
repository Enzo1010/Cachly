import { Routes } from '@angular/router';

export const ROTAS_DASHBOARD: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/dashboard/dashboard.component').then(
        (componente) => componente.DashboardComponent,
      ),
    title: 'Visão Geral | Questly',
  },
  {
    path: 'estudar',
    loadComponent: () =>
      import('./pages/pagina-titulo/pagina-titulo.component').then(
        (componente) => componente.PaginaTituloComponent,
      ),
    data: { titulo: 'Estudar' },
    title: 'Estudar | Questly',
  },
  {
    path: 'desempenho',
    loadComponent: () =>
      import('./pages/pagina-titulo/pagina-titulo.component').then(
        (componente) => componente.PaginaTituloComponent,
      ),
    data: { titulo: 'Desempenho' },
    title: 'Desempenho | Questly',
  },
  {
    path: 'liga-semanal',
    loadComponent: () =>
      import('./pages/pagina-titulo/pagina-titulo.component').then(
        (componente) => componente.PaginaTituloComponent,
      ),
    data: { titulo: 'Liga Semanal' },
    title: 'Liga Semanal | Questly',
  },
  {
    path: 'conquistas',
    loadComponent: () =>
      import('./pages/pagina-titulo/pagina-titulo.component').then(
        (componente) => componente.PaginaTituloComponent,
      ),
    data: { titulo: 'Conquistas' },
    title: 'Conquistas | Questly',
  },
  {
    path: 'perfil',
    loadComponent: () =>
      import('./pages/perfil/perfil.component').then((componente) => componente.PerfilComponent),
    title: 'Perfil | Questly',
  },
];

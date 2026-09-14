import { Routes } from '@angular/router';

export const ROTAS_ADMIN: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/visao-geral/visao-geral-admin.component').then(
        (m) => m.VisaoGeralAdminComponent
      ),
    data: { titulo: 'Visão Geral do Administrador' },
    title: 'Visão Geral | Admin Cachly',
  },
  {
    path: 'questoes',
    loadComponent: () =>
      import('./pages/questoes/gerenciar-questoes.component').then(
        (m) => m.GerenciarQuestoesComponent
      ),
    data: { titulo: 'Gerenciamento de Questões' },
    title: 'Questões | Admin Cachly',
  },
  {
    path: 'categorias',
    loadComponent: () =>
      import('./pages/categorias/gerenciar-categorias.component').then(
        (m) => m.GerenciarCategoriasComponent
      ),
    data: { titulo: 'Gerenciamento de Categorias' },
    title: 'Categorias | Admin Cachly',
  },
];

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
  {
    path: 'desafios',
    loadComponent: () =>
      import('./pages/desafios-simulador/admin-desafios-simulador-list.component').then(
        (m) => m.AdminDesafiosSimuladorListComponent
      ),
    data: { titulo: 'Desafios do Simulador' },
    title: 'Desafios | Admin Cachly',
  },
  {
    path: 'desafios/novo',
    loadComponent: () =>
      import('./pages/desafios-simulador/admin-desafios-simulador-form.component').then(
        (m) => m.AdminDesafiosSimuladorFormComponent
      ),
    data: { titulo: 'Novo Desafio do Simulador' },
    title: 'Novo Desafio | Admin Cachly',
  },
  {
    path: 'desafios/editar/:id',
    loadComponent: () =>
      import('./pages/desafios-simulador/admin-desafios-simulador-form.component').then(
        (m) => m.AdminDesafiosSimuladorFormComponent
      ),
    data: { titulo: 'Editar Desafio do Simulador' },
    title: 'Editar Desafio | Admin Cachly',
  },
];

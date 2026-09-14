import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  CategoriaAdminRequest,
  CategoriaAdminResponse,
  QuestaoAdminRequest,
  QuestaoAdminResponse,
} from '../models/admin.model';

@Injectable({ providedIn: 'root' })
export class AdminApiService {
  private readonly http = inject(HttpClient);

  // Questões
  listarQuestoes(): Observable<QuestaoAdminResponse[]> {
    return this.http.get<QuestaoAdminResponse[]>('/api/questoes/admin/todas');
  }

  cadastrarQuestao(dados: QuestaoAdminRequest): Observable<QuestaoAdminResponse> {
    return this.http.post<QuestaoAdminResponse>('/api/questoes', dados);
  }

  atualizarQuestao(id: number, dados: QuestaoAdminRequest): Observable<QuestaoAdminResponse> {
    return this.http.put<QuestaoAdminResponse>(`/api/questoes/${id}`, dados);
  }

  desativarQuestao(id: number): Observable<QuestaoAdminResponse> {
    return this.http.patch<QuestaoAdminResponse>(`/api/questoes/${id}/desativar`, {});
  }

  ativarQuestao(id: number): Observable<QuestaoAdminResponse> {
    return this.http.patch<QuestaoAdminResponse>(`/api/questoes/${id}/ativar`, {});
  }

  // Categorias
  listarCategorias(): Observable<CategoriaAdminResponse[]> {
    return this.http.get<CategoriaAdminResponse[]>('/api/categorias/admin/todas');
  }

  listarCategoriasAtivas(): Observable<CategoriaAdminResponse[]> {
    return this.http.get<CategoriaAdminResponse[]>('/api/categorias');
  }

  cadastrarCategoria(dados: CategoriaAdminRequest): Observable<CategoriaAdminResponse> {
    return this.http.post<CategoriaAdminResponse>('/api/categorias', dados);
  }

  atualizarCategoria(id: number, dados: CategoriaAdminRequest): Observable<CategoriaAdminResponse> {
    return this.http.put<CategoriaAdminResponse>(`/api/categorias/${id}`, dados);
  }

  desativarCategoria(id: number): Observable<CategoriaAdminResponse> {
    return this.http.patch<CategoriaAdminResponse>(`/api/categorias/${id}/desativar`, {});
  }

  ativarCategoria(id: number): Observable<CategoriaAdminResponse> {
    return this.http.patch<CategoriaAdminResponse>(`/api/categorias/${id}/ativar`, {});
  }
}

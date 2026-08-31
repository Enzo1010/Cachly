import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  CategoriaResponse,
  QuestaoEstudoResponse,
  RespostaRequest,
  RespostaResponse
} from '../models/estudar.model';

@Injectable({ providedIn: 'root' })
export class EstudarApiService {
  private readonly http = inject(HttpClient);

  listarCategorias(): Observable<CategoriaResponse[]> {
    return this.http.get<CategoriaResponse[]>('/api/categorias');
  }

  listarQuestoes(categoriaId?: number, limite: number = 10): Observable<QuestaoEstudoResponse[]> {
    let params = new HttpParams().set('limite', limite);
    if (categoriaId) {
      params = params.set('categoriaId', categoriaId);
    }
    return this.http.get<QuestaoEstudoResponse[]>('/api/questoes/estudo', { params });
  }

  responderQuestao(questaoId: number, alternativaId: number): Observable<RespostaResponse> {
    const request: RespostaRequest = { alternativaId };
    return this.http.post<RespostaResponse>(`/api/questoes/${questaoId}/respostas`, request);
  }
}

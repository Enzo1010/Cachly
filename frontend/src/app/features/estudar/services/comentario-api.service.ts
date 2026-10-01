import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface ComentarioResponse {
  id: number;
  conteudo: string;
  upvotes: number;
  criadoEm: string;
  autorNome: string;
  autorPerfil: string;
  usuarioDeuUpvote: boolean;
}

@Injectable({ providedIn: 'root' })
export class ComentarioApiService {
  private readonly http = inject(HttpClient);

  listarPorQuestao(questaoId: number): Observable<ComentarioResponse[]> {
    return this.http.get<ComentarioResponse[]>(`/api/questoes/${questaoId}/comentarios`);
  }

  adicionarComentario(questaoId: number, conteudo: string): Observable<ComentarioResponse> {
    return this.http.post<ComentarioResponse>(`/api/questoes/${questaoId}/comentarios`, { conteudo });
  }

  darUpvote(comentarioId: number): Observable<void> {
    return this.http.post<void>(`/api/questoes/comentarios/${comentarioId}/upvote`, {});
  }
}

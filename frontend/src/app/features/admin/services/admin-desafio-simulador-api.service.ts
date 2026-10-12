import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DesafioCacheResponse, DesafioSimuladorRequest } from '../models/admin-desafio.model';

@Injectable({
  providedIn: 'root'
})
export class AdminDesafioSimuladorApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/admin/desafios-simulador';

  listarTodos(): Observable<DesafioCacheResponse[]> {
    return this.http.get<DesafioCacheResponse[]>(this.apiUrl);
  }

  obterPorId(id: string): Observable<DesafioCacheResponse> {
    return this.http.get<DesafioCacheResponse>(`${this.apiUrl}/${id}`);
  }

  criar(desafio: DesafioSimuladorRequest): Observable<DesafioCacheResponse> {
    return this.http.post<DesafioCacheResponse>(this.apiUrl, desafio);
  }

  atualizar(id: string, desafio: DesafioSimuladorRequest): Observable<DesafioCacheResponse> {
    return this.http.put<DesafioCacheResponse>(`${this.apiUrl}/${id}`, desafio);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

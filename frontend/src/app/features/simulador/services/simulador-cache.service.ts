import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { SimulacaoRequest, SimulacaoResponse } from '../models/simulador.model';
import { DesafioCache, ResultadoDesafio, VerificarDesafioRequest } from '../models/desafio.model';

@Injectable({
  providedIn: 'root'
})
export class SimuladorCacheService {
  private readonly http = inject(HttpClient);

  executarSimulacao(request: SimulacaoRequest): Observable<SimulacaoResponse> {
    return this.http.post<SimulacaoResponse>('/api/simulador/executar', request);
  }

  listarDesafios(): Observable<readonly DesafioCache[]> {
    return this.http.get<readonly DesafioCache[]>('/api/simulador/desafios');
  }

  verificarDesafio(id: string, request: VerificarDesafioRequest): Observable<ResultadoDesafio> {
    return this.http.post<ResultadoDesafio>(`/api/simulador/desafios/${id}/verificar`, request);
  }
}

import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { SimulacaoRequest, SimulacaoResponse } from '../models/simulador.model';

@Injectable({
  providedIn: 'root'
})
export class SimuladorCacheService {
  private readonly http = inject(HttpClient);

  executarSimulacao(request: SimulacaoRequest): Observable<SimulacaoResponse> {
    return this.http.post<SimulacaoResponse>('/api/simulador/executar', request);
  }
}

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { DesempenhoResponse } from '../models/desempenho.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);

  obterDesempenho(): Observable<DesempenhoResponse> {
    return this.http.get<DesempenhoResponse>('/api/alunos/me/desempenho');
  }
}

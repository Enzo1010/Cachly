import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { PaginaRanking } from '../models/ranking.model';

@Injectable({ providedIn: 'root' })
export class LigaSemanalService {
  private readonly http = inject(HttpClient);

  obterRanking(): Observable<PaginaRanking> {
    return this.http.get<PaginaRanking>('/api/ranking');
  }
}

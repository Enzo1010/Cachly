import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { PaginaRanking } from '../models/ranking.model';

@Injectable({ providedIn: 'root' })
export class LigaSemanalService {
  private readonly http = inject(HttpClient);

  /**
   * Busca os 5 primeiros colocados do ranking, que é a mesma quantidade de vagas
   * para avançar de liga usada nesta tela.
   *
   * O endpoint GET /api/ranking hoje ordena por xpTotal (XP acumulado do aluno),
   * não existe ainda no backend um XP específico da semana nem o conceito de
   * "liga" (Prata, Ouro, etc). Por isso essa lista é a fonte real de dados, mas
   * o cartão de liga logo acima dela ainda é um placeholder (ver liga-semanal.component.ts).
   */
  obterRanking(): Observable<PaginaRanking> {
    return this.http.get<PaginaRanking>('/api/ranking', { params: { size: 5 } });
  }
}

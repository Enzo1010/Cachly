import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { AlterarSenhaRequest } from '../models/perfil.model';

@Injectable({ providedIn: 'root' })
export class PerfilApiService {
  private readonly http = inject(HttpClient);

  alterarSenha(request: AlterarSenhaRequest): Observable<void> {
    return this.http.post<void>('/api/auth/alterar-senha', request);
  }
}

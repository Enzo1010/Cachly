import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { AlunoCadastrado, CadastroAlunoRequest } from '../models/cadastro-aluno.model';

@Injectable({ providedIn: 'root' })
export class CadastroAlunoService {
  private readonly http = inject(HttpClient);

  cadastrar(request: CadastroAlunoRequest): Observable<AlunoCadastrado> {
    return this.http.post<AlunoCadastrado>('/api/alunos', request);
  }
}

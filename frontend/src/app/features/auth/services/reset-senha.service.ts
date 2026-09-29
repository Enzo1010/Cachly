import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ResetSenhaService {
  private readonly http = inject(HttpClient);

  solicitarReset(email: string): Observable<void> {
    return this.http.post<void>('/api/auth/solicitar-reset', { email });
  }

  resetarSenha(token: string, novaSenha: string): Observable<void> {
    return this.http.post<void>('/api/auth/resetar-senha', { token, novaSenha });
  }
}

import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, tap, catchError, of } from 'rxjs';

import { AutenticacaoRequest, UsuarioAutenticado } from './autenticacao.model';

@Injectable({ providedIn: 'root' })
export class SessaoService {
  private readonly http = inject(HttpClient);
  private readonly usuarioAtual = signal<UsuarioAutenticado | null>(null);

  readonly usuario = this.usuarioAtual.asReadonly();
  readonly estaAutenticado = computed(() => this.usuarioAtual() !== null);

  carregarSessao(): Observable<UsuarioAutenticado | null> {
    return this.http.get<UsuarioAutenticado>('/api/auth/me').pipe(
      tap((usuario) => this.usuarioAtual.set(usuario)),
      catchError(() => {
        this.usuarioAtual.set(null);
        return of(null);
      })
    );
  }

  atualizarAposResposta(xpTotal: number, nivel: number, nomeNivel: string): void {
    const atual = this.usuarioAtual();
    if (atual) {
      this.usuarioAtual.set({
        ...atual,
        xpTotal,
        nivel,
        nomeNivel
      });
    }
  }

  autenticar(request: AutenticacaoRequest, lembrarLogin: boolean): Observable<UsuarioAutenticado> {
    const payload = { ...request, lembrarLogin };
    return this.http
      .post<UsuarioAutenticado>('/api/auth/login', payload)
      .pipe(tap((usuario) => this.usuarioAtual.set(usuario)));
  }

  limparSessaoLocal(): void {
    this.usuarioAtual.set(null);
  }

  encerrar(): Observable<void> {
    return this.http.post<void>('/api/auth/logout', {}).pipe(
      tap(() => this.usuarioAtual.set(null)),
      catchError(() => {
        this.usuarioAtual.set(null);
        return of(void 0);
      })
    );
  }
}

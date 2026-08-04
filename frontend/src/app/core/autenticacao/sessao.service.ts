import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { AutenticacaoRequest, UsuarioAutenticado } from './autenticacao.model';

@Injectable({ providedIn: 'root' })
export class SessaoService {
  private readonly http = inject(HttpClient);
  private readonly chaveArmazenamento = 'questly.usuario-autenticado';
  private readonly usuarioAtual = signal<UsuarioAutenticado | null>(this.recuperarUsuario());

  readonly usuario = this.usuarioAtual.asReadonly();
  readonly estaAutenticado = computed(() => this.usuarioAtual() !== null);

  autenticar(request: AutenticacaoRequest, lembrarLogin: boolean): Observable<UsuarioAutenticado> {
    return this.http
      .post<UsuarioAutenticado>('/api/auth/login', request)
      .pipe(tap((usuario) => this.iniciarSessao(usuario, lembrarLogin)));
  }

  encerrar(): void {
    window.localStorage.removeItem(this.chaveArmazenamento);
    window.sessionStorage.removeItem(this.chaveArmazenamento);
    this.usuarioAtual.set(null);
  }

  private iniciarSessao(usuario: UsuarioAutenticado, lembrarLogin: boolean): void {
    this.encerrar();

    const armazenamento = lembrarLogin ? window.localStorage : window.sessionStorage;
    armazenamento.setItem(this.chaveArmazenamento, JSON.stringify(usuario));
    this.usuarioAtual.set(usuario);
  }

  private recuperarUsuario(): UsuarioAutenticado | null {
    const usuarioArmazenado =
      window.localStorage.getItem(this.chaveArmazenamento) ??
      window.sessionStorage.getItem(this.chaveArmazenamento);

    if (!usuarioArmazenado) {
      return null;
    }

    try {
      const usuario = JSON.parse(usuarioArmazenado) as unknown;
      return this.ehUsuarioAutenticado(usuario) ? usuario : null;
    } catch {
      window.localStorage.removeItem(this.chaveArmazenamento);
      window.sessionStorage.removeItem(this.chaveArmazenamento);
      return null;
    }
  }

  private ehUsuarioAutenticado(valor: unknown): valor is UsuarioAutenticado {
    if (typeof valor !== 'object' || valor === null) {
      return false;
    }

    return (
      'id' in valor &&
      typeof valor.id === 'number' &&
      'nome' in valor &&
      typeof valor.nome === 'string' &&
      'email' in valor &&
      typeof valor.email === 'string' &&
      'perfil' in valor &&
      (valor.perfil === 'ALUNO' || valor.perfil === 'ADMINISTRADOR') &&
      'xpTotal' in valor &&
      typeof valor.xpTotal === 'number' &&
      'nivel' in valor &&
      typeof valor.nivel === 'number'
    );
  }
}

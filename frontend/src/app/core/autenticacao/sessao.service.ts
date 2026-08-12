import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { AutenticacaoRequest, UsuarioAutenticado } from './autenticacao.model';

@Injectable({ providedIn: 'root' })
export class SessaoService {
  private readonly http = inject(HttpClient);
  private readonly chaveArmazenamento = 'cachly.usuario-autenticado';
  private readonly chaveLoginLembrado = 'cachly.login-lembrado';
  private readonly usuarioAtual = signal<UsuarioAutenticado | null>(this.recuperarUsuario());

  readonly usuario = this.usuarioAtual.asReadonly();
  readonly estaAutenticado = computed(() => this.usuarioAtual() !== null);

  autenticar(request: AutenticacaoRequest, lembrarLogin: boolean): Observable<UsuarioAutenticado> {
    this.atualizarLoginLembrado(request, lembrarLogin);

    return this.http
      .post<UsuarioAutenticado>('/api/auth/login', request)
      .pipe(tap((usuario) => this.iniciarSessao(usuario, lembrarLogin)));
  }

  encerrar(): void {
    window.localStorage.removeItem(this.chaveArmazenamento);
    window.sessionStorage.removeItem(this.chaveArmazenamento);
    this.usuarioAtual.set(null);
  }

  obterLoginLembrado(): AutenticacaoRequest | null {
    const loginArmazenado = window.localStorage.getItem(this.chaveLoginLembrado);

    if (!loginArmazenado) {
      return null;
    }

    try {
      const login = JSON.parse(loginArmazenado) as unknown;

      if (this.ehLoginLembrado(login)) {
        return login;
      }
    } catch {
      // O armazenamento será limpo abaixo.
    }

    window.localStorage.removeItem(this.chaveLoginLembrado);
    return null;
  }

  private iniciarSessao(usuario: UsuarioAutenticado, lembrarLogin: boolean): void {
    this.encerrar();

    const armazenamento = lembrarLogin ? window.localStorage : window.sessionStorage;
    armazenamento.setItem(this.chaveArmazenamento, JSON.stringify(usuario));
    this.usuarioAtual.set(usuario);
  }

  private atualizarLoginLembrado(request: AutenticacaoRequest, lembrarLogin: boolean): void {
    if (lembrarLogin) {
      window.localStorage.setItem(this.chaveLoginLembrado, JSON.stringify(request));
      return;
    }

    window.localStorage.removeItem(this.chaveLoginLembrado);
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
      typeof valor.nivel === 'number' &&
      'token' in valor &&
      typeof valor.token === 'string' &&
      valor.token.length > 0
    );
  }

  private ehLoginLembrado(valor: unknown): valor is AutenticacaoRequest {
    return (
      typeof valor === 'object' &&
      valor !== null &&
      'email' in valor &&
      typeof valor.email === 'string' &&
      'senha' in valor &&
      typeof valor.senha === 'string'
    );
  }
}

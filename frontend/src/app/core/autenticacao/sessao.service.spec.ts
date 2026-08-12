import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { configurarArmazenamentosTeste } from '../../../testing/armazenamento-teste';
import { SessaoService } from './sessao.service';

describe('SessaoService', () => {
  beforeEach(() => {
    configurarArmazenamentosTeste();
    window.localStorage.clear();
    window.sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
  });

  afterEach(() => {
    TestBed.inject(HttpTestingController).verify();
  });

  it('deve manter a sessao apenas durante a aba quando lembrar login estiver desmarcado', () => {
    const service = TestBed.inject(SessaoService);
    const http = TestBed.inject(HttpTestingController);

    service
      .autenticar({ email: 'ana.silva@exemplo.com', senha: 'senha-segura' }, false)
      .subscribe();

    http.expectOne('/api/auth/login').flush(criarUsuario());

    expect(service.estaAutenticado()).toBe(true);
    expect(window.sessionStorage.getItem('cachly.usuario-autenticado')).toContain('Ana Silva');
    expect(window.localStorage.getItem('cachly.usuario-autenticado')).toBeNull();
    expect(window.localStorage.getItem('cachly.login-lembrado')).toBeNull();
  });

  it('deve persistir a sessao quando lembrar login estiver marcado', () => {
    const service = TestBed.inject(SessaoService);
    const http = TestBed.inject(HttpTestingController);

    service.autenticar({ email: 'ana.silva@exemplo.com', senha: 'senha-segura' }, true).subscribe();

    expect(window.localStorage.getItem('cachly.login-lembrado')).toBe(
      JSON.stringify({ email: 'ana.silva@exemplo.com', senha: 'senha-segura' }),
    );

    http.expectOne('/api/auth/login').flush(criarUsuario());

    expect(window.localStorage.getItem('cachly.usuario-autenticado')).toContain('Ana Silva');
    expect(window.sessionStorage.getItem('cachly.usuario-autenticado')).toBeNull();
  });

  it('deve limpar a sessao e preservar o login lembrado ao encerrar', () => {
    window.localStorage.setItem('cachly.usuario-autenticado', JSON.stringify(criarUsuario()));
    window.localStorage.setItem(
      'cachly.login-lembrado',
      JSON.stringify({ email: 'ana.silva@exemplo.com', senha: 'senha-segura' }),
    );
    const service = TestBed.inject(SessaoService);

    service.encerrar();

    expect(service.estaAutenticado()).toBe(false);
    expect(window.localStorage.getItem('cachly.usuario-autenticado')).toBeNull();
    expect(service.obterLoginLembrado()).toEqual({
      email: 'ana.silva@exemplo.com',
      senha: 'senha-segura',
    });
  });

  function criarUsuario() {
    return {
      id: 1,
      nome: 'Ana Silva',
      email: 'ana.silva@exemplo.com',
      perfil: 'ALUNO' as const,
      xpTotal: 0,
      nivel: 1,
      token: 'token-de-teste',
    };
  }
});

import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { SessaoService } from './sessao.service';

describe('SessaoService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
  });

  afterEach(() => {
    TestBed.inject(HttpTestingController).verify();
  });

  it('deve autenticar o usuario chamando a api e guardando estado em memoria', () => {
    const service = TestBed.inject(SessaoService);
    const http = TestBed.inject(HttpTestingController);

    service
      .autenticar({ email: 'ana.silva@exemplo.com', senha: 'senha-segura' }, false)
      .subscribe();

    http.expectOne('/api/auth/login').flush(criarUsuario());

    expect(service.estaAutenticado()).toBe(true);
    expect(service.usuario()?.nome).toBe('Ana Silva');
  });

  it('deve carregar a sessao via chamando /api/auth/me', () => {
    const service = TestBed.inject(SessaoService);
    const http = TestBed.inject(HttpTestingController);

    service.carregarSessao().subscribe();

    http.expectOne('/api/auth/me').flush(criarUsuario());

    expect(service.estaAutenticado()).toBe(true);
    expect(service.usuario()?.nome).toBe('Ana Silva');
  });

  it('deve encerrar a sessao fazendo chamada para /api/auth/logout', () => {
    const service = TestBed.inject(SessaoService);
    const http = TestBed.inject(HttpTestingController);

    // Initial load
    service.carregarSessao().subscribe();
    http.expectOne('/api/auth/me').flush(criarUsuario());
    expect(service.estaAutenticado()).toBe(true);

    // Logout
    service.encerrar().subscribe();
    http.expectOne('/api/auth/logout').flush({});

    expect(service.estaAutenticado()).toBe(false);
    expect(service.usuario()).toBeNull();
  });

  function criarUsuario() {
    return {
      id: 1,
      nome: 'Ana Silva',
      email: 'ana.silva@exemplo.com',
      perfil: 'ALUNO' as const,
      xpTotal: 0,
      nivel: 1,
      diasOfensiva: 0,
      token: 'token-de-teste',
    };
  }
});

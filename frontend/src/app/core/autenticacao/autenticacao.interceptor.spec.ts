import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { SessaoService } from './sessao.service';
import { autenticacaoInterceptor } from './autenticacao.interceptor';
import { vi, describe, beforeEach, afterEach, it, expect } from 'vitest';

describe('autenticacaoInterceptor', () => {
  let httpTestingController: HttpTestingController;
  let httpClient: HttpClient;
  let sessaoServiceMock: { limparSessaoLocal: ReturnType<typeof vi.fn> };
  let routerMock: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    sessaoServiceMock = { limparSessaoLocal: vi.fn() };
    routerMock = { navigate: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([autenticacaoInterceptor])),
        provideHttpClientTesting(),
        { provide: SessaoService, useValue: sessaoServiceMock },
        { provide: Router, useValue: routerMock }
      ]
    });

    httpTestingController = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('deve adicionar withCredentials: true nas requisições', () => {
    httpClient.get('/api/teste').subscribe();

    const req = httpTestingController.expectOne('/api/teste');
    expect(req.request.withCredentials).toBe(true);
    req.flush({});
  });

  it('Cenário A: deve deslogar e redirecionar para /login ao receber 401 de uma rota normal (ex: /api/estudo/resumo)', () => {
    httpClient.get('/api/estudo/resumo').subscribe({
      error: () => {} // Ignora o erro no subscribe
    });

    const req = httpTestingController.expectOne('/api/estudo/resumo');
    
    // Simula resposta 401 do backend
    req.flush({ error: 'Unauthorized', timestamp: new Date().toISOString() }, { status: 401, statusText: 'Unauthorized' });

    expect(sessaoServiceMock.limparSessaoLocal).toHaveBeenCalled();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('Cenário B: NÃO deve deslogar nem redirecionar ao receber 401 de /api/auth/alterar-senha', () => {
    httpClient.post('/api/auth/alterar-senha', {}).subscribe({
      error: () => {}
    });

    const req = httpTestingController.expectOne('/api/auth/alterar-senha');
    
    req.flush({ mensagem: 'Senha atual incorreta' }, { status: 401, statusText: 'Unauthorized' });

    expect(sessaoServiceMock.limparSessaoLocal).not.toHaveBeenCalled();
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  it('Cenário B: NÃO deve deslogar nem redirecionar ao receber 401 de /api/auth/login', () => {
    httpClient.post('/api/auth/login', {}).subscribe({
      error: () => {}
    });

    const req = httpTestingController.expectOne('/api/auth/login');
    
    req.flush({ mensagem: 'Credenciais inválidas' }, { status: 401, statusText: 'Unauthorized' });

    expect(sessaoServiceMock.limparSessaoLocal).not.toHaveBeenCalled();
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  it('não deve fazer nada se o erro não for 401', () => {
    httpClient.get('/api/teste').subscribe({
      error: () => {}
    });

    const req = httpTestingController.expectOne('/api/teste');
    
    req.flush({}, { status: 403, statusText: 'Forbidden' });

    expect(sessaoServiceMock.limparSessaoLocal).not.toHaveBeenCalled();
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });
});

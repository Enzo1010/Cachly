import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  provideRouter,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';

import { alunoGuard } from './aluno.guard';
import { SessaoService } from './sessao.service';
import { UsuarioAutenticado } from './autenticacao.model';

describe('alunoGuard', () => {
  it('deve permitir acesso para usuario com perfil ALUNO', () => {
    configurarTeste(true, {
      id: 2,
      nome: 'Aluno',
      email: 'aluno@teste.com',
      perfil: 'ALUNO',
      xpTotal: 100,
      nivel: 1,
      diasOfensiva: 1,
      nomeNivel: 'Iniciante',
    });

    const resultado = executarGuard();
    expect(resultado).toBe(true);
  });

  it('deve redirecionar para /admin quando usuario for ADMINISTRADOR', () => {
    configurarTeste(true, {
      id: 1,
      nome: 'Admin',
      email: 'admin@cachly.com',
      perfil: 'ADMINISTRADOR',
      xpTotal: 0,
      nivel: 1,
      diasOfensiva: 0,
      nomeNivel: 'Admin',
    });

    const router = TestBed.inject(Router);
    const resultado = executarGuard();

    expect(resultado).toBeInstanceOf(UrlTree);
    expect(router.serializeUrl(resultado as UrlTree)).toBe('/admin');
  });

  it('deve redirecionar para /login quando nao houver sessao', () => {
    configurarTeste(false, null);

    const router = TestBed.inject(Router);
    const resultado = executarGuard();

    expect(resultado).toBeInstanceOf(UrlTree);
    expect(router.serializeUrl(resultado as UrlTree)).toBe('/login');
  });

  function configurarTeste(autenticado: boolean, usuario: UsuarioAutenticado | null): void {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: SessaoService,
          useValue: {
            estaAutenticado: signal(autenticado),
            usuario: signal(usuario),
          },
        },
      ],
    });
  }

  function executarGuard(): boolean | UrlTree {
    return TestBed.runInInjectionContext(
      () =>
        alunoGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot) as
          boolean | UrlTree,
    );
  }
});

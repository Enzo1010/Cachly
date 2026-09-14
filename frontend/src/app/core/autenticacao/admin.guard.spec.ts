import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  provideRouter,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';

import { adminGuard } from './admin.guard';
import { SessaoService } from './sessao.service';
import { UsuarioAutenticado } from './autenticacao.model';

describe('adminGuard', () => {
  it('deve permitir acesso para usuario com perfil ADMINISTRADOR', () => {
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

    const resultado = executarGuard();
    expect(resultado).toBe(true);
  });

  it('deve redirecionar para /dashboard quando usuario for ALUNO', () => {
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

    const router = TestBed.inject(Router);
    const resultado = executarGuard();

    expect(resultado).toBeInstanceOf(UrlTree);
    expect(router.serializeUrl(resultado as UrlTree)).toBe('/dashboard');
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
        adminGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot) as
          boolean | UrlTree,
    );
  }
});

import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  provideRouter,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';

import { autenticacaoGuard } from './autenticacao.guard';
import { SessaoService } from './sessao.service';

describe('autenticacaoGuard', () => {
  it('deve permitir o acesso quando houver usuario autenticado', () => {
    configurarTeste(true);

    const resultado = executarGuard();

    expect(resultado).toBe(true);
  });

  it('deve redirecionar para o login quando nao houver sessao', () => {
    configurarTeste(false);
    const router = TestBed.inject(Router);

    const resultado = executarGuard();

    expect(resultado).toBeInstanceOf(UrlTree);
    expect(router.serializeUrl(resultado as UrlTree)).toBe('/login');
  });

  function configurarTeste(autenticado: boolean): void {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: SessaoService,
          useValue: { estaAutenticado: signal(autenticado) },
        },
      ],
    });
  }

  function executarGuard(): boolean | UrlTree {
    return TestBed.runInInjectionContext(
      () =>
        autenticacaoGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot) as
          boolean | UrlTree,
    );
  }
});

import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { SessaoService } from '../autenticacao/sessao.service';
import { LayoutPrincipalComponent } from './layout-principal.component';

describe('LayoutPrincipalComponent', () => {
  it('deve encerrar a sessao e navegar para o login', () => {
    const encerrar = vi.fn();
    TestBed.configureTestingModule({
      imports: [LayoutPrincipalComponent],
      providers: [
        provideRouter([]),
        {
          provide: SessaoService,
          useValue: {
            encerrar,
            usuario: signal({
              id: 1,
              nome: 'Ana Silva',
              email: 'ana.silva@exemplo.com',
              perfil: 'ALUNO',
              xpTotal: 0,
              nivel: 1,
              token: 'token-de-teste',
            }),
          },
        },
      ],
    });

    const fixture = TestBed.createComponent(LayoutPrincipalComponent);
    const router = TestBed.inject(Router);
    const navegar = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    fixture.detectChanges();

    const botao = fixture.nativeElement.querySelector('.botao-sair') as HTMLButtonElement;
    botao.click();

    expect(encerrar).toHaveBeenCalledOnce();
    expect(navegar).toHaveBeenCalledWith('/login');
  });
});

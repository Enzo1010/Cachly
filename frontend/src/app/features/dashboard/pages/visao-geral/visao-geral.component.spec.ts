import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SessaoService } from '../../../../core/autenticacao/sessao.service';
import { VisaoGeralComponent } from './visao-geral.component';

describe('VisaoGeralComponent', () => {
  let fixture: ComponentFixture<VisaoGeralComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VisaoGeralComponent],
      providers: [
        {
          provide: SessaoService,
          useValue: {
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
    }).compileComponents();

    fixture = TestBed.createComponent(VisaoGeralComponent);
    fixture.detectChanges();
  });

  it('deve exibir o resumo da visao geral', () => {
    const conteudo = fixture.nativeElement as HTMLElement;

    expect(conteudo.querySelector('h2')?.textContent).toContain('Olá, Ana Silva!');
    expect(conteudo.querySelectorAll('.indicador')).toHaveLength(3);
    expect(conteudo.textContent).toContain('Seu próximo objetivo');
  });
});

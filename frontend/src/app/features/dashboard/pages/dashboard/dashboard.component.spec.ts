import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SessaoService } from '../../../../core/autenticacao/sessao.service';
import { DashboardComponent } from './dashboard.component';

describe('DashboardComponent', () => {
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
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
            }),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
  });

  it('should display the dashboard summary', () => {
    const conteudo = fixture.nativeElement as HTMLElement;

    expect(conteudo.querySelector('h2')?.textContent).toContain('Olá, Ana Silva!');
    expect(conteudo.querySelectorAll('.indicador')).toHaveLength(3);
    expect(conteudo.textContent).toContain('Seu próximo objetivo');
  });
});

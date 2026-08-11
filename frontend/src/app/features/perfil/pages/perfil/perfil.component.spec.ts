import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { SessaoService } from '../../../../core/autenticacao/sessao.service';
import { PerfilComponent } from './perfil.component';

describe('PerfilComponent', () => {
  let fixture: ComponentFixture<PerfilComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PerfilComponent],
      providers: [
        provideRouter([]),
        {
          provide: SessaoService,
          useValue: {
            encerrar: vi.fn(),
            usuario: signal({
              id: 1,
              nome: 'Ezequiel',
              email: 'ezequielhgm@gmail.com',
              perfil: 'ALUNO',
              xpTotal: 0,
              nivel: 1,
              token: 'token-de-teste',
            }),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PerfilComponent);
    fixture.detectChanges();
  });

  it('deve exibir o resumo do usuario autenticado', () => {
    const conteudo = fixture.nativeElement as HTMLElement;

    expect(conteudo.querySelector('#titulo-perfil')?.textContent).toContain('Ezequiel');
    expect(conteudo.textContent).toContain('Aluno Cachly');
    expect(conteudo.textContent).toContain('0 XP total');
    expect(conteudo.textContent).toMatch(/N.{1,3}vel 1/);
  });

  it('deve abrir a edicao com os dados atuais do usuario', () => {
    const botao = fixture.nativeElement.querySelector('.botao-editar') as HTMLButtonElement;
    botao.click();
    fixture.detectChanges();

    const nome = fixture.nativeElement.querySelector('#nome-perfil') as HTMLInputElement;
    const email = fixture.nativeElement.querySelector('#email-perfil') as HTMLInputElement;

    expect(nome.value).toBe('Ezequiel');
    expect(email.value).toBe('ezequielhgm@gmail.com');
  });
});

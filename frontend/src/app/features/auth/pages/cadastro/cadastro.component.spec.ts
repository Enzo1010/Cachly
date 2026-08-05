import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { MessageService } from 'primeng/api';

import { CadastroComponent } from './cadastro.component';

describe('CadastroComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CadastroComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        MessageService,
      ],
    }).compileComponents();
  });

  afterEach(() => {
    TestBed.inject(HttpTestingController).verify();
  });

  it('deve criar a tela de cadastro', () => {
    const fixture = TestBed.createComponent(CadastroComponent);

    expect(fixture.componentInstance).toBeTruthy();
  });

  it('deve cadastrar o aluno e direcionar para o login', () => {
    const fixture = TestBed.createComponent(CadastroComponent);
    const router = TestBed.inject(Router);
    const http = TestBed.inject(HttpTestingController);
    const notificacoes = TestBed.inject(MessageService);
    const navegar = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    const notificar = vi.spyOn(notificacoes, 'add');
    fixture.detectChanges();

    preencherFormulario(fixture.nativeElement as HTMLElement);
    (fixture.nativeElement.querySelector('form') as HTMLFormElement).dispatchEvent(
      new Event('submit'),
    );

    const requisicao = http.expectOne('/api/alunos');
    expect(requisicao.request.method).toBe('POST');
    expect(requisicao.request.body).toEqual({
      nome: 'Ana Silva',
      email: 'ana.silva@exemplo.com',
      senha: 'senha-segura',
    });
    requisicao.flush({ id: 1, nome: 'Ana Silva', email: 'ana.silva@exemplo.com' });

    expect(navegar).toHaveBeenCalledWith('/login');
    expect(notificar).toHaveBeenCalledWith({
      severity: 'success',
      summary: 'Cadastro realizado',
      detail: 'Sua conta foi criada. Agora entre para continuar.',
      life: 5000,
    });
  });

  it('nao deve cadastrar quando o formulario estiver invalido', () => {
    const fixture = TestBed.createComponent(CadastroComponent);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    (fixture.nativeElement.querySelector('form') as HTMLFormElement).dispatchEvent(
      new Event('submit'),
    );
    fixture.detectChanges();

    http.expectNone('/api/alunos');
    expect(fixture.nativeElement.textContent).toContain('Informe seu nome.');
  });

  function preencherFormulario(conteudo: HTMLElement): void {
    preencherCampo(conteudo, '#nome', 'Ana Silva');
    preencherCampo(conteudo, '#email', 'ana.silva@exemplo.com');
    preencherCampo(conteudo, '#senha', 'senha-segura');
  }

  function preencherCampo(conteudo: HTMLElement, seletor: string, valor: string): void {
    const campo = conteudo.querySelector(seletor) as HTMLInputElement;
    campo.value = valor;
    campo.dispatchEvent(new Event('input'));
  }
});

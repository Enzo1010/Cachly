import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { MessageService } from 'primeng/api';

import { configurarArmazenamentosTeste } from '../../../../../testing/armazenamento-teste';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  beforeEach(async () => {
    configurarArmazenamentosTeste();
    window.localStorage.clear();
    window.sessionStorage.clear();

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
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

  it('deve criar a tela de login', () => {
    const fixture = TestBed.createComponent(LoginComponent);

    expect(fixture.componentInstance).toBeTruthy();
  });

  it('deve alternar a visibilidade da senha', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();

    const botao = fixture.nativeElement.querySelector('.botao-visibilidade') as HTMLButtonElement;
    const senha = fixture.nativeElement.querySelector('#senha') as HTMLInputElement;

    expect(senha.type).toBe('password');
    botao.click();
    fixture.detectChanges();
    expect(senha.type).toBe('text');
  });

  it('deve exibir as opcoes de lembrar login e recuperar senha', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();

    const conteudo = fixture.nativeElement as HTMLElement;
    expect(conteudo.querySelector('#lembrar-login')).toBeTruthy();
    expect(conteudo.textContent).toContain('Esqueci minha senha');
  });

  it('deve disponibilizar o link para a tela de cadastro', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();

    const link = fixture.nativeElement.querySelector(
      'a[routerLink="/cadastro"]',
    ) as HTMLAnchorElement;

    expect(link).toBeTruthy();
    expect(link.getAttribute('href')).toBe('/cadastro');
  });

  it('deve autenticar e navegar para o dashboard', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const router = TestBed.inject(Router);
    const http = TestBed.inject(HttpTestingController);
    const navegar = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    fixture.detectChanges();

    preencherFormulario(fixture.nativeElement as HTMLElement);
    const formulario = fixture.nativeElement.querySelector('form') as HTMLFormElement;
    formulario.dispatchEvent(new Event('submit'));

    const requisicao = http.expectOne('/api/auth/login');
    expect(requisicao.request.method).toBe('POST');
    expect(requisicao.request.body).toEqual({
      email: 'ana.silva@exemplo.com',
      senha: 'senha-segura',
    });
    requisicao.flush(criarUsuario());

    expect(navegar).toHaveBeenCalledWith('/dashboard');
    expect(window.sessionStorage.getItem('questly.usuario-autenticado')).toContain('Ana Silva');
  });

  it('deve exibir o erro devolvido pelo backend', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const http = TestBed.inject(HttpTestingController);
    const notificacoes = TestBed.inject(MessageService);
    const notificar = vi.spyOn(notificacoes, 'add');
    fixture.detectChanges();

    preencherFormulario(fixture.nativeElement as HTMLElement);
    const formulario = fixture.nativeElement.querySelector('form') as HTMLFormElement;
    formulario.dispatchEvent(new Event('submit'));

    http
      .expectOne('/api/auth/login')
      .flush(
        { mensagem: 'E-mail ou senha inválidos' },
        { status: 401, statusText: 'Unauthorized' },
      );
    fixture.detectChanges();

    const mensagem = fixture.nativeElement.querySelector('.mensagem-login') as HTMLElement;
    expect(mensagem.textContent).toContain('E-mail ou senha inválidos');
    expect(notificar).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'Não foi possível entrar',
      detail: 'E-mail ou senha inválidos',
      life: 5000,
    });
  });

  it('nao deve chamar a API quando o formulario estiver invalido', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    const formulario = fixture.nativeElement.querySelector('form') as HTMLFormElement;
    formulario.dispatchEvent(new Event('submit'));
    fixture.detectChanges();

    http.expectNone('/api/auth/login');
    expect(fixture.nativeElement.textContent).toContain('Informe seu e-mail.');
  });

  function preencherFormulario(conteudo: HTMLElement): void {
    const email = conteudo.querySelector('#email') as HTMLInputElement;
    const senha = conteudo.querySelector('#senha') as HTMLInputElement;

    email.value = 'ana.silva@exemplo.com';
    email.dispatchEvent(new Event('input'));
    senha.value = 'senha-segura';
    senha.dispatchEvent(new Event('input'));
  }

  function criarUsuario() {
    return {
      id: 1,
      nome: 'Ana Silva',
      email: 'ana.silva@exemplo.com',
      perfil: 'ALUNO' as const,
      xpTotal: 0,
      nivel: 1,
      token: 'token-de-teste',
    };
  }
});

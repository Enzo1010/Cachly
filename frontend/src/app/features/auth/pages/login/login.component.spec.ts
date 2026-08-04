import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [provideRouter([])],
    }).compileComponents();
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

  it('deve navegar para o dashboard ao enviar o formulario', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const router = TestBed.inject(Router);
    const navegar = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    fixture.detectChanges();

    const formulario = fixture.nativeElement.querySelector('form') as HTMLFormElement;
    formulario.dispatchEvent(new Event('submit'));

    expect(navegar).toHaveBeenCalledWith('/dashboard');
  });
});

import { TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
    }).compileComponents();
  });

  it('deve criar a tela de login', () => {
    const fixture = TestBed.createComponent(LoginComponent);

    expect(fixture.componentInstance).toBeTruthy();
  });

  it('deve informar os campos obrigatorios ao enviar o formulario vazio', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();

    const formulario = fixture.nativeElement.querySelector('form') as HTMLFormElement;
    formulario.dispatchEvent(new Event('submit'));
    fixture.detectChanges();

    const conteudo = (fixture.nativeElement as HTMLElement).textContent;
    expect(conteudo).toContain('Informe seu e-mail.');
    expect(conteudo).toContain('Informe sua senha.');
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
});

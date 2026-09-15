import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { MessageService } from 'primeng/api';
import { vi, describe, it, expect, beforeEach } from 'vitest';

import { SessaoService } from '../../../../core/autenticacao/sessao.service';
import { PerfilApiService } from '../../services/perfil-api.service';
import { PerfilComponent } from './perfil.component';

describe('PerfilComponent', () => {
  let component: PerfilComponent;
  let fixture: ComponentFixture<PerfilComponent>;
  let mockPerfilApi: { alterarSenha: ReturnType<typeof vi.fn> };
  let mockSessaoService: { encerrar: ReturnType<typeof vi.fn>; usuario: ReturnType<typeof signal> };
  let mockMessageService: { add: ReturnType<typeof vi.fn> };
  let router: Router;

  beforeEach(async () => {
    mockPerfilApi = {
      alterarSenha: vi.fn().mockReturnValue(of(void 0)),
    };

    mockSessaoService = {
      encerrar: vi.fn().mockReturnValue(of(void 0)),
      usuario: signal({
        id: 1,
        nome: 'Ezequiel',
        email: 'ezequielhgm@gmail.com',
        perfil: 'ALUNO',
        xpTotal: 0,
        nivel: 1,
        token: 'token-de-teste',
      }),
    };

    mockMessageService = {
      add: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [PerfilComponent],
      providers: [
        provideRouter([]),
        { provide: PerfilApiService, useValue: mockPerfilApi },
        { provide: SessaoService, useValue: mockSessaoService },
        { provide: MessageService, useValue: mockMessageService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PerfilComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
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

  it('deve invalidar o formulário quando as senhas não coincidirem ou forem menores que 8 caracteres', () => {
    component['abrirEdicao']();

    component['formularioEdicao'].patchValue({
      senhaAtual: 'senha123',
      novaSenha: 'curto',
      confirmacaoNovaSenha: 'curto',
    });
    expect(component['formularioEdicao'].controls.novaSenha.errors?.['minlength']).toBeTruthy();

    component['formularioEdicao'].patchValue({
      novaSenha: 'novaSenha123',
      confirmacaoNovaSenha: 'outraSenha123',
    });
    expect(component['formularioEdicao'].errors?.['senhasDiferentes']).toBeTruthy();

    component['formularioEdicao'].patchValue({
      novaSenha: 'novaSenha123',
      confirmacaoNovaSenha: 'novaSenha123',
    });
    expect(component['formularioEdicao'].valid).toBe(true);
  });

  it('deve executar a alteração de senha com sucesso, exibir toast, fechar modal e redirecionar para login', () => {
    const navigateSpy = vi.spyOn(router, 'navigateByUrl');
    component['abrirEdicao']();

    component['formularioEdicao'].patchValue({
      senhaAtual: 'senhaAntiga123',
      novaSenha: 'novaSenha123',
      confirmacaoNovaSenha: 'novaSenha123',
    });

    component['salvarAlteracaoSenha']();

    expect(mockPerfilApi.alterarSenha).toHaveBeenCalledWith({
      senhaAtual: 'senhaAntiga123',
      novaSenha: 'novaSenha123',
    });

    expect(mockMessageService.add).toHaveBeenCalledWith(
      expect.objectContaining({
        severity: 'success',
        summary: 'Senha alterada com sucesso!',
      })
    );

    expect(mockSessaoService.encerrar).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith('/login');
    expect(component['edicaoAberta']()).toBe(false);
  });

  it('deve tratar erro 401 quando a senha atual estiver incorreta e exibir mensagem clara', () => {
    const erro401 = new HttpErrorResponse({
      status: 401,
      statusText: 'Unauthorized',
      error: {
        timestamp: new Date().toISOString(),
        status: 401,
        mensagem: 'Senha atual incorreta',
      },
    });

    mockPerfilApi.alterarSenha.mockReturnValue(throwError(() => erro401));
    component['abrirEdicao']();

    component['formularioEdicao'].patchValue({
      senhaAtual: 'senhaErrada',
      novaSenha: 'novaSenha123',
      confirmacaoNovaSenha: 'novaSenha123',
    });

    component['salvarAlteracaoSenha']();

    expect(mockPerfilApi.alterarSenha).toHaveBeenCalled();
    expect(component['mensagemErro']()).toBe('Senha atual incorreta');
    expect(mockMessageService.add).toHaveBeenCalledWith(
      expect.objectContaining({
        severity: 'error',
        detail: 'Senha atual incorreta',
      })
    );
    expect(component['salvando']()).toBe(false);
    expect(component['edicaoAberta']()).toBe(true);
  });
});

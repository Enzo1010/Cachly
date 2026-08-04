import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonDirective, ButtonIcon, ButtonLabel } from 'primeng/button';
import { Checkbox } from 'primeng/checkbox';
import { InputText } from 'primeng/inputtext';
import { finalize } from 'rxjs';

import { SessaoService } from '../../../../core/autenticacao/sessao.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, InputText, Checkbox, ButtonDirective, ButtonIcon, ButtonLabel],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly router = inject(Router);
  private readonly sessao = inject(SessaoService);

  protected readonly senhaVisivel = signal(false);
  protected readonly carregando = signal(false);
  protected readonly erroLogin = signal('');

  protected readonly formularioLogin = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    senha: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(6)],
    }),
    lembrarLogin: new FormControl(false, { nonNullable: true }),
  });

  protected alternarVisibilidadeSenha(): void {
    this.senhaVisivel.update((visivel) => !visivel);
  }

  protected entrar(): void {
    if (this.formularioLogin.invalid || this.carregando()) {
      this.formularioLogin.markAllAsTouched();
      return;
    }

    const { email, senha, lembrarLogin } = this.formularioLogin.getRawValue();
    this.erroLogin.set('');
    this.carregando.set(true);

    this.sessao
      .autenticar({ email, senha }, lembrarLogin)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: () => void this.router.navigateByUrl('/dashboard'),
        error: (erro: unknown) => this.erroLogin.set(this.obterMensagemErro(erro)),
      });
  }

  private obterMensagemErro(erro: unknown): string {
    if (!(erro instanceof HttpErrorResponse)) {
      return 'Não foi possível entrar. Tente novamente.';
    }

    if (erro.status === 0) {
      return 'Não foi possível conectar ao servidor.';
    }

    const resposta = erro.error as unknown;
    if (
      typeof resposta === 'object' &&
      resposta !== null &&
      'mensagem' in resposta &&
      typeof resposta.mensagem === 'string'
    ) {
      return resposta.mensagem;
    }

    return 'Não foi possível entrar. Tente novamente.';
  }
}

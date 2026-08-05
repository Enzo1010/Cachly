import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ButtonDirective, ButtonIcon, ButtonLabel } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { MessageService } from 'primeng/api';
import { finalize } from 'rxjs';

import { CadastroAlunoService } from '../../services/cadastro-aluno.service';

@Component({
  selector: 'app-cadastro',
  imports: [ReactiveFormsModule, RouterLink, InputText, ButtonDirective, ButtonIcon, ButtonLabel],
  templateUrl: './cadastro.component.html',
  styleUrl: './cadastro.component.scss',
})
export class CadastroComponent {
  private readonly router = inject(Router);
  private readonly cadastroAluno = inject(CadastroAlunoService);
  private readonly notificacoes = inject(MessageService);

  protected readonly senhaVisivel = signal(false);
  protected readonly carregando = signal(false);
  protected readonly erroCadastro = signal('');

  protected readonly formularioCadastro = new FormGroup({
    nome: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(100)],
    }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email, Validators.maxLength(150)],
    }),
    senha: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(8), Validators.maxLength(72)],
    }),
  });

  protected alternarVisibilidadeSenha(): void {
    this.senhaVisivel.update((visivel) => !visivel);
  }

  protected cadastrar(): void {
    if (this.formularioCadastro.invalid || this.carregando()) {
      this.formularioCadastro.markAllAsTouched();
      return;
    }

    const { nome, email, senha } = this.formularioCadastro.getRawValue();
    this.erroCadastro.set('');
    this.carregando.set(true);

    this.cadastroAluno
      .cadastrar({ nome, email, senha })
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: () => {
          this.notificacoes.add({
            severity: 'success',
            summary: 'Cadastro realizado',
            detail: 'Sua conta foi criada. Agora entre para continuar.',
            life: 5000,
          });
          void this.router.navigateByUrl('/login');
        },
        error: (erro: unknown) => this.erroCadastro.set(this.obterMensagemErro(erro)),
      });
  }

  private obterMensagemErro(erro: unknown): string {
    if (!(erro instanceof HttpErrorResponse)) {
      return 'Não foi possível criar sua conta. Tente novamente.';
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

    return 'Não foi possível criar sua conta. Tente novamente.';
  }
}

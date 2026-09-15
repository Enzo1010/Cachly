import { Component, computed, inject, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { ButtonDirective, ButtonIcon, ButtonLabel } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { ToggleSwitch } from 'primeng/toggleswitch';

import { SessaoService } from '../../../../core/autenticacao/sessao.service';
import { PerfilApiService } from '../../services/perfil-api.service';

function senhasIguaisValidator(group: AbstractControl): ValidationErrors | null {
  const novaSenha = group.get('novaSenha')?.value;
  const confirmacao = group.get('confirmacaoNovaSenha')?.value;
  if (novaSenha && confirmacao && novaSenha !== confirmacao) {
    return { senhasDiferentes: true };
  }
  return null;
}

@Component({
  selector: 'app-perfil',
  imports: [
    ReactiveFormsModule,
    ButtonDirective,
    ButtonIcon,
    ButtonLabel,
    Dialog,
    InputText,
    MessageModule,
    ToggleSwitch,
  ],
  templateUrl: './perfil.component.html',
  styleUrl: './perfil.component.scss',
})
export class PerfilComponent {
  private readonly router = inject(Router);
  protected readonly sessao = inject(SessaoService);
  private readonly perfilApi = inject(PerfilApiService);
  private readonly notificacoes = inject(MessageService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);

  protected readonly edicaoAberta = signal(false);
  protected readonly salvando = signal(false);
  protected readonly mensagemErro = signal<string | null>(null);

  protected readonly usuario = this.sessao.usuario;
  protected readonly iniciais = computed(() => {
    const nome = this.usuario()?.nome.trim() ?? '';
    const partes = nome.split(/\s+/).filter(Boolean);

    return partes.length > 1
      ? `${partes[0].charAt(0)}${partes.at(-1)?.charAt(0)}`.toUpperCase()
      : nome.charAt(0).toUpperCase() || 'U';
  });

  protected readonly formularioConfiguracoes = new FormGroup({
    notificacoesEstudo: new FormControl(true, { nonNullable: true }),
    perfilPublico: new FormControl(true, { nonNullable: true }),
  });

  protected readonly formularioEdicao = this.fb.nonNullable.group(
    {
      nome: [''],
      email: [''],
      senhaAtual: ['', [Validators.required]],
      novaSenha: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
      confirmacaoNovaSenha: ['', [Validators.required]],
    },
    {
      validators: [senhasIguaisValidator],
    }
  );

  protected abrirEdicao(): void {
    const usuario = this.usuario();
    this.formularioEdicao.reset({
      nome: usuario?.nome ?? '',
      email: usuario?.email ?? '',
      senhaAtual: '',
      novaSenha: '',
      confirmacaoNovaSenha: '',
    });
    this.mensagemErro.set(null);
    this.edicaoAberta.set(true);
  }

  protected fecharEdicao(): void {
    this.edicaoAberta.set(false);
    this.mensagemErro.set(null);
  }

  protected salvarAlteracaoSenha(): void {
    if (this.formularioEdicao.invalid || this.salvando()) {
      this.formularioEdicao.markAllAsTouched();
      return;
    }

    const { senhaAtual, novaSenha } = this.formularioEdicao.getRawValue();

    this.salvando.set(true);
    this.mensagemErro.set(null);

    this.perfilApi
      .alterarSenha({ senhaAtual, novaSenha })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.salvando.set(false);
          this.fecharEdicao();
          this.notificacoes.add({
            severity: 'success',
            summary: 'Senha alterada com sucesso!',
            detail: 'Sua sessão foi encerrada por segurança. Por favor, faça login com sua nova senha.',
            life: 5000,
          });
          this.sessao
            .encerrar()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(() => {
              void this.router.navigateByUrl('/login');
            });
        },
        error: (err: HttpErrorResponse) => {
          this.salvando.set(false);
          let mensagem = 'Ocorreu um erro ao alterar a senha.';
          if (err.status === 401) {
            mensagem = err.error?.mensagem || 'Senha atual incorreta.';
          } else if (err.status === 400 && err.error?.campos) {
            const campos = Object.values(err.error.campos).join(' | ');
            mensagem = campos || err.error?.mensagem || 'Dados inválidos.';
          } else if (err.error?.mensagem) {
            mensagem = err.error.mensagem;
          }
          this.mensagemErro.set(mensagem);
          this.notificacoes.add({
            severity: 'error',
            summary: 'Erro ao alterar senha',
            detail: mensagem,
            life: 4000,
          });
        },
      });
  }

  protected sair(): void {
    this.sessao.encerrar()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        void this.router.navigateByUrl('/login');
      });
  }
}

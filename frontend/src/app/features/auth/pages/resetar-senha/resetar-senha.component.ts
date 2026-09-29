import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  inject,
  signal,
  DestroyRef,
  OnInit,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ButtonDirective, ButtonIcon, ButtonLabel } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { finalize } from 'rxjs';

import { ResetSenhaService } from '../../services/reset-senha.service';

function senhasIguaisValidator(group: AbstractControl): ValidationErrors | null {
  const novaSenha = group.get('novaSenha')?.value;
  const confirmacao = group.get('confirmacaoNovaSenha')?.value;
  if (novaSenha && confirmacao && novaSenha !== confirmacao) {
    return { senhasDiferentes: true };
  }
  return null;
}

@Component({
  selector: 'app-resetar-senha',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    InputText,
    ButtonDirective,
    ButtonIcon,
    ButtonLabel,
    MessageModule,
  ],
  templateUrl: './resetar-senha.component.html',
  styleUrl: './resetar-senha.component.scss',
})
export class ResetarSenhaComponent implements OnInit {
  private readonly resetService = inject(ResetSenhaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly salvando = signal(false);
  protected readonly sucesso = signal(false);
  protected readonly mensagemErro = signal<string | null>(null);
  protected readonly tokenInvalido = signal(false);

  private token = '';

  protected readonly formulario = new FormGroup(
    {
      novaSenha: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(8), Validators.maxLength(72)],
      }),
      confirmacaoNovaSenha: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required],
      }),
    },
    {
      validators: [senhasIguaisValidator],
    }
  );

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) {
      this.tokenInvalido.set(true);
    }
  }

  protected resetar(): void {
    if (this.formulario.invalid || this.salvando()) {
      this.formulario.markAllAsTouched();
      return;
    }

    const { novaSenha } = this.formulario.getRawValue();
    this.salvando.set(true);
    this.mensagemErro.set(null);

    this.resetService
      .resetarSenha(this.token, novaSenha)
      .pipe(
        finalize(() => this.salvando.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => this.sucesso.set(true),
        error: (err: HttpErrorResponse) => {
          if (err.status === 401) {
            this.tokenInvalido.set(true);
          } else {
            const resposta = err.error as Record<string, unknown> | null;
            this.mensagemErro.set(
              (typeof resposta?.['mensagem'] === 'string' ? resposta['mensagem'] : null) ??
                'Ocorreu um erro ao redefinir a senha. Tente novamente.'
            );
          }
        },
      });
  }
}

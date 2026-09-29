import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonDirective, ButtonIcon, ButtonLabel } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { finalize } from 'rxjs';

import { ResetSenhaService } from '../../services/reset-senha.service';

@Component({
  selector: 'app-esqueci-senha',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    InputText,
    ButtonDirective,
    ButtonIcon,
    ButtonLabel,
  ],
  templateUrl: './esqueci-senha.component.html',
  styleUrl: './esqueci-senha.component.scss',
})
export class EsqueciSenhaComponent {
  private readonly resetService = inject(ResetSenhaService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly enviando = signal(false);
  protected readonly enviado = signal(false);
  protected readonly formulario = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
  });

  protected solicitar(): void {
    if (this.formulario.invalid || this.enviando()) {
      this.formulario.markAllAsTouched();
      return;
    }

    const { email } = this.formulario.getRawValue();
    this.enviando.set(true);

    this.resetService
      .solicitarReset(email)
      .pipe(
        finalize(() => this.enviando.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => this.enviado.set(true),
        error: () => this.enviado.set(true), // Always show success to prevent email enumeration
      });
  }
}

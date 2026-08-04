import { Component, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  protected readonly senhaVisivel = signal(false);
  protected readonly formularioEnviado = signal(false);

  protected readonly formularioLogin = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    senha: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(6)],
    }),
  });

  protected alternarVisibilidadeSenha(): void {
    this.senhaVisivel.update((visivel) => !visivel);
  }

  protected entrar(): void {
    this.formularioEnviado.set(false);
    this.formularioLogin.markAllAsTouched();

    if (this.formularioLogin.invalid) {
      return;
    }

    this.formularioEnviado.set(true);
  }
}

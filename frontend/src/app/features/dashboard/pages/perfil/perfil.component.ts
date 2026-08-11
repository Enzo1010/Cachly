import { Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonDirective, ButtonIcon, ButtonLabel } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { ToggleSwitch } from 'primeng/toggleswitch';

import { SessaoService } from '../../../../core/autenticacao/sessao.service';

@Component({
  selector: 'app-perfil',
  imports: [
    ReactiveFormsModule,
    ButtonDirective,
    ButtonIcon,
    ButtonLabel,
    Dialog,
    InputText,
    ToggleSwitch,
  ],
  templateUrl: './perfil.component.html',
  styleUrl: './perfil.component.scss',
})
export class PerfilComponent {
  private readonly router = inject(Router);
  protected readonly sessao = inject(SessaoService);

  protected readonly edicaoAberta = signal(false);
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

  protected readonly formularioEdicao = new FormGroup({
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
      validators: [Validators.minLength(8), Validators.maxLength(72)],
    }),
  });

  protected abrirEdicao(): void {
    const usuario = this.usuario();
    this.formularioEdicao.reset({
      nome: usuario?.nome ?? '',
      email: usuario?.email ?? '',
      senha: '',
    });
    this.edicaoAberta.set(true);
  }

  protected fecharEdicao(): void {
    this.edicaoAberta.set(false);
  }

  protected sair(): void {
    this.sessao.encerrar();
    void this.router.navigateByUrl('/login');
  }
}

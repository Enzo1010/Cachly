import { Component, HostListener, signal } from '@angular/core';
import { DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  ActivatedRoute,
  NavigationEnd,
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from '@angular/router';
import { ButtonDirective } from 'primeng/button';
import { filter } from 'rxjs';

import { SessaoService } from '../autenticacao/sessao.service';

interface ItemNavegacao {
  readonly icone: string;
  readonly rotulo: string;
  readonly rota: string;
}

@Component({
  selector: 'app-layout-principal',
  imports: [ButtonDirective, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './layout-principal.component.html',
  styleUrl: './layout-principal.component.scss',
})
export class LayoutPrincipalComponent {
  private readonly router = inject(Router);
  private readonly rotaAtiva = inject(ActivatedRoute);
  private readonly destruir = inject(DestroyRef);
  protected readonly sessao = inject(SessaoService);

  protected readonly menuAberto = signal(false);
  protected readonly sidebarRecolhida = signal(false);
  protected readonly tituloPagina = signal('Visão Geral');
  protected readonly menuUsuarioAberto = signal(false);

  protected readonly navegacaoPrincipal: readonly ItemNavegacao[] = [
    { icone: 'pi pi-home', rotulo: 'Início', rota: '/dashboard' },
    { icone: 'pi pi-server', rotulo: 'Simulador', rota: '/simulador' },
    { icone: 'pi pi-book', rotulo: 'Estudar', rota: '/estudar' },
    { icone: 'pi pi-chart-pie', rotulo: 'Desempenho', rota: '/desempenho' },
  ];

  constructor() {
    this.atualizarTituloPagina();

    this.router.events
      .pipe(
        filter((evento): evento is NavigationEnd => evento instanceof NavigationEnd),
        takeUntilDestroyed(this.destruir),
      )
      .subscribe(() => {
        this.atualizarTituloPagina();
        this.menuUsuarioAberto.set(false);
      });
  }

  @HostListener('document:click', ['$event'])
  protected aoClicarFora(event: MouseEvent): void {
    const alvo = event.target as HTMLElement | null;
    if (!alvo?.closest('.usuario-menu-wrapper')) {
      this.menuUsuarioAberto.set(false);
    }
  }

  protected alternarMenu(): void {
    if (window.innerWidth <= 800) {
      this.menuAberto.update((aberto) => !aberto);
    } else {
      this.sidebarRecolhida.update((rec) => !rec);
    }
  }

  protected alternarMenuUsuario(event: MouseEvent): void {
    event.stopPropagation();
    this.menuUsuarioAberto.update((aberto) => !aberto);
  }

  protected fecharMenuUsuario(): void {
    this.menuUsuarioAberto.set(false);
  }

  protected fecharMenu(): void {
    this.menuAberto.set(false);
  }

  protected sair(): void {
    this.fecharMenu();
    this.sessao.encerrar()
      .pipe(takeUntilDestroyed(this.destruir))
      .subscribe(() => {
        void this.router.navigateByUrl('/login');
      });
  }

  private atualizarTituloPagina(): void {
    let rota: ActivatedRoute | null = this.rotaAtiva;

    while (rota?.firstChild) {
      rota = rota.firstChild;
    }

    const titulo = rota?.snapshot?.data?.['titulo'];
    this.tituloPagina.set(typeof titulo === 'string' ? titulo : 'Visão Geral');
  }
}

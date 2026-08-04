import { Component, signal } from '@angular/core';
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

  protected readonly menuAberto = signal(false);
  protected readonly tituloPagina = signal('Visão Geral');

  protected readonly navegacaoPrincipal: readonly ItemNavegacao[] = [
    { icone: 'pi pi-home', rotulo: 'Início', rota: '/dashboard' },
    { icone: 'pi pi-book', rotulo: 'Estudar', rota: '/dashboard/estudar' },
    { icone: 'pi pi-chart-pie', rotulo: 'Desempenho', rota: '/dashboard/desempenho' },
    { icone: 'pi pi-trophy', rotulo: 'Liga Semanal', rota: '/dashboard/liga-semanal' },
    { icone: 'pi pi-verified', rotulo: 'Conquistas', rota: '/dashboard/conquistas' },
  ];


  constructor() {
    this.atualizarTituloPagina();

    this.router.events
      .pipe(
        filter((evento): evento is NavigationEnd => evento instanceof NavigationEnd),
        takeUntilDestroyed(this.destruir),
      )
      .subscribe(() => this.atualizarTituloPagina());
  }

  protected alternarMenu(): void {
    this.menuAberto.update((aberto) => !aberto);
  }

  protected fecharMenu(): void {
    this.menuAberto.set(false);
  }

  protected sair(): void {
    this.fecharMenu();
    void this.router.navigateByUrl('/login');
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

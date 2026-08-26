import { Component, inject, OnInit, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MessageService } from 'primeng/api';
import { SkeletonModule } from 'primeng/skeleton';
import { Conquista } from '../../models/conquista.model';
import { ConquistasService } from '../../services/conquistas.service';

@Component({
  selector: 'app-conquistas',
  imports: [SkeletonModule],
  templateUrl: './conquistas.component.html',
  styleUrl: './conquistas.component.scss',
})
export class ConquistasComponent implements OnInit {
  private readonly conquistasService = inject(ConquistasService);
  private readonly messageService = inject(MessageService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly conquistas = signal<readonly Conquista[]>([]);
  protected readonly carregando = signal(true);

  ngOnInit(): void {
    this.conquistasService.obterConquistas()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
      next: (dados) => {
        this.conquistas.set(dados);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Erro',
          detail: 'Falha ao carregar conquistas.',
        });
      },
    });
  }
}

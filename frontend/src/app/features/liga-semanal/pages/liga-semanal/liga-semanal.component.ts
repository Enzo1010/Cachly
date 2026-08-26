import { Component, computed, inject, signal, OnInit, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MessageService } from 'primeng/api';
import { SkeletonModule } from 'primeng/skeleton';
import { PaginaRanking } from '../../models/ranking.model';
import { SessaoService } from '../../../../core/autenticacao/sessao.service';
import { LigaSemanalService } from '../../services/liga-semanal.service';

interface PrazoRestante {
  readonly dias: number;
  readonly horas: number;
}

@Component({
  selector: 'app-liga-semanal',
  imports: [SkeletonModule],
  templateUrl: './liga-semanal.component.html',
  styleUrl: './liga-semanal.component.scss',
})
export class LigaSemanalComponent implements OnInit {
  protected readonly sessao = inject(SessaoService);
  private readonly ligaSemanalService = inject(LigaSemanalService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly messageService = inject(MessageService);

  protected readonly ranking = signal<PaginaRanking | null>(null);
  protected readonly alunos = computed(() => this.ranking()?.content ?? []);
  protected readonly carregando = signal(true);

  protected readonly nomeLiga = computed(() => this.sessao.usuario()?.nomeNivel ?? 'Liga Iniciante');
  protected readonly proximaLiga = computed(() => {
    const nivelAtual = this.sessao.usuario()?.nivel ?? 1;
    return `Nível ${nivelAtual + 1}`;
  });
  protected readonly vagasParaAvancar = 5;
  protected readonly prazoRestante = signal<PrazoRestante>(this.calcularPrazoRestante());

  ngOnInit(): void {
    // Atualiza o prazo a cada 1 minuto
    const intervalId = setInterval(() => {
      this.prazoRestante.set(this.calcularPrazoRestante());
    }, 60000);

    // Garante que o timer seja limpo quando o componente for destruído (evita vazamento de memória)
    this.destroyRef.onDestroy(() => clearInterval(intervalId));

    this.ligaSemanalService.obterRanking()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
      next: (dados) => {
        this.ranking.set(dados);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Erro',
          detail: 'Falha ao carregar ranking.',
        });
      },
    });
  }

  protected ehUsuarioAtual(nome: string): boolean {
    return nome === this.sessao.usuario()?.nome;
  }

  protected iconePosicao(posicao: number): 'trophy' | 'verified' | null {
    if (posicao === 1) {
      return 'trophy';
    }

    if (posicao === 2 || posicao === 3) {
      return 'verified';
    }

    return null;
  }

  private calcularPrazoRestante(): PrazoRestante {
    const agora = new Date();
    const fimDaSemana = this.obterFimDaSemana(agora);
    const diferencaEmMs = Math.max(fimDaSemana.getTime() - agora.getTime(), 0);

    const milissegundosPorHora = 1000 * 60 * 60;
    const dias = Math.floor(diferencaEmMs / (milissegundosPorHora * 24));
    const horas = Math.floor((diferencaEmMs % (milissegundosPorHora * 24)) / milissegundosPorHora);

    return { dias, horas };
  }

  private obterFimDaSemana(referencia: Date): Date {
    const fim = new Date(referencia);
    const diasAteDomingo = (7 - fim.getDay()) % 7;

    fim.setDate(fim.getDate() + diasAteDomingo);
    fim.setHours(23, 59, 59, 999);

    return fim;
  }
}
